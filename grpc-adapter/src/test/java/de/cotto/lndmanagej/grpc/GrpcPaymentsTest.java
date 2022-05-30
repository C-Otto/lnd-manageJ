package de.cotto.lndmanagej.grpc;

import com.google.protobuf.ByteString;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.HexString;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RouteHint;
import lnrpc.HTLCAttempt;
import lnrpc.Hop;
import lnrpc.HopHint;
import lnrpc.ListPaymentsResponse;
import lnrpc.PayReq;
import lnrpc.Payment.PaymentStatus;
import lnrpc.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcPaymentsTest {
    private static final long ADD_INDEX_OFFSET = 123;
    private static final int LIMIT = 1_000;
    private static final String PAYMENT_REQUEST = "abc";

    @InjectMocks
    private GrpcPayments grpcPayments;

    @Mock
    private GrpcService grpcService;

    @Test
    void empty_optional() {
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.empty());
        assertThat(grpcPayments.getPaymentsAfter(0L)).isEmpty();
    }

    @Test
    void no_payment() {
        ListPaymentsResponse response = ListPaymentsResponse.newBuilder().build();
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcPayments.getPaymentsAfter(0L)).contains(List.of());
    }

    @Test
    void with_payments() {
        ListPaymentsResponse response = ListPaymentsResponse.newBuilder()
                .addPayments(payment(PaymentStatus.SUCCEEDED, PAYMENT))
                .addPayments(payment(PaymentStatus.SUCCEEDED, PAYMENT_2))
                .build();
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcPayments.getPaymentsAfter(0L)).contains(
                List.of(PAYMENT, PAYMENT_2)
        );
    }

    @Test
    void skips_unsuccessful_htlcs() {
        Route route = Route.newBuilder().addHops(Hop.getDefaultInstance()).build();
        HTLCAttempt htlc  = HTLCAttempt.newBuilder()
                .setRoute(route)
                .setStatus(HTLCAttempt.HTLCStatus.IN_FLIGHT)
                .build();
        lnrpc.Payment payment = lnrpc.Payment.newBuilder()
                .setStatus(PaymentStatus.SUCCEEDED)
                .addHtlcs(htlc)
                .build();

        ListPaymentsResponse response = ListPaymentsResponse.newBuilder()
                .addPayments(payment)
                .build();
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcPayments.getPaymentsAfter(0L).orElseThrow().stream().map(Payment::routes))
                .contains(List.of());
    }

    @Test
    void payment_without_channel_id() {
        Hop hop = Hop.newBuilder()
                .setChanId(0L)
                .setAmtToForwardMsat(123L)
                .build();
        Route route = Route.newBuilder().addHops(hop).build();
        HTLCAttempt htlc = HTLCAttempt.newBuilder().setStatus(HTLCAttempt.HTLCStatus.SUCCEEDED).setRoute(route).build();
        lnrpc.Payment result = lnrpc.Payment.newBuilder()
                .setStatus(PaymentStatus.SUCCEEDED)
                .setPaymentIndex(PAYMENT.index())
                .setPaymentHash(PAYMENT.paymentHash())
                .setValueMsat(PAYMENT.value().milliSatoshis())
                .setFeeMsat(PAYMENT.fees().milliSatoshis())
                .setCreationTimeNs(PAYMENT.creationDateTime().toInstant(ZoneOffset.UTC).toEpochMilli() * 1_000)
                .addHtlcs(htlc)
                .build();
        ListPaymentsResponse response = ListPaymentsResponse.newBuilder()
                .addPayments(result)
                .build();
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.of(response));
        assertThat(grpcPayments.getPaymentsAfter(0L).orElseThrow().get(0).routes()).hasSize(1);
    }

    @Test
    void throws_exception_for_unsuccessful_payment() {
        mockResponse(payment(PaymentStatus.IN_FLIGHT, null));
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(
                () -> grpcPayments.getPaymentsAfter(0L)
        );
    }

    private void mockResponse(lnrpc.Payment payment) {
        when(grpcService.getPayments(anyLong(), anyInt())).thenReturn(Optional.of(ListPaymentsResponse.newBuilder()
                .addPayments(payment)
                .build()));
    }

    @Test
    void starts_at_the_beginning() {
        grpcPayments.getPaymentsAfter(ADD_INDEX_OFFSET);
        verify(grpcService).getPayments(eq(ADD_INDEX_OFFSET), anyInt());
    }

    @Test
    void uses_limit() {
        grpcPayments.getPaymentsAfter(ADD_INDEX_OFFSET);
        verify(grpcService).getPayments(ADD_INDEX_OFFSET, LIMIT);
    }

    @Test
    void getLimit() {
        assertThat(grpcPayments.getLimit()).isEqualTo(LIMIT);
    }

    @Test
    void decodePaymentRequest_empty() {
        assertThat(grpcPayments.decodePaymentRequest(PAYMENT_REQUEST)).isEmpty();
    }

    @Test
    void decodePaymentRequest() {
        String paymentHashHex = "aabbcc";
        HexString paymentAddress = new HexString("dd00");
        int cltvExpiry = 111;
        String description = "description";
        PayReq payReq = PayReq.newBuilder()
                .setDestination(PUBKEY_4.toString())
                .setNumMsat(1234)
                .setDescription(description)
                .setPaymentHash(paymentHashHex)
                .setPaymentAddr(ByteString.copyFrom(paymentAddress.getByteArray()))
                .setCltvExpiry(cltvExpiry)
                .setTimestamp(100)
                .setExpiry(99)
                .addRouteHints(routeHint(CHANNEL_ID, 9, Coins.NONE, PUBKEY, 123))
                .addRouteHints(routeHint(CHANNEL_ID_2, 40, Coins.ofMilliSatoshis(1), PUBKEY_3, 1234))
                .build();
        when(grpcService.decodePaymentRequest(PAYMENT_REQUEST)).thenReturn(Optional.of(payReq));
        Coins amount = Coins.ofMilliSatoshis(1234);
        HexString paymentHash = new HexString(paymentHashHex);
        Instant creation = Instant.ofEpochSecond(100);
        Instant expiry = Instant.ofEpochSecond(199);
        Set<RouteHint> routeHints = DECODED_PAYMENT_REQUEST.routeHints();
        assertThat(grpcPayments.decodePaymentRequest(PAYMENT_REQUEST)).contains(new DecodedPaymentRequest(
                PAYMENT_REQUEST,
                cltvExpiry,
                description,
                PUBKEY_4,
                amount,
                paymentHash,
                paymentAddress,
                creation,
                expiry,
                routeHints
        ));
    }

    private lnrpc.RouteHint routeHint(
            ChannelId channelId,
            int cltvExpiryDelta,
            Coins baseFee,
            Pubkey pubkey,
            int feeRate
    ) {
        return lnrpc.RouteHint.newBuilder().addHopHints(hopHint(
                channelId,
                cltvExpiryDelta,
                baseFee,
                pubkey,
                feeRate
        )).build();
    }

    private HopHint hopHint(ChannelId channelId, int cltvExpiryDelta, Coins baseFee, Pubkey node, int feeRate) {
        return HopHint.newBuilder()
                .setCltvExpiryDelta(cltvExpiryDelta)
                .setFeeBaseMsat((int) baseFee.milliSatoshis())
                .setNodeId(node.toString())
                .setFeeProportionalMillionths(feeRate)
                .setChanId(channelId.getShortChannelId())
                .build();
    }

    private lnrpc.Payment payment(
            PaymentStatus status,
            @Nullable Payment payment
    ) {
        if (payment == null) {
            return lnrpc.Payment.newBuilder().setStatus(status).build();
        }
        HTLCAttempt.Builder htlcBuilder = HTLCAttempt.newBuilder();
        List<HTLCAttempt> htlcs = new ArrayList<>();
        for (PaymentRoute paymentRoute : payment.routes()) {
            Route.Builder routeBuilder = Route.newBuilder();
            for (PaymentHop hop : paymentRoute.hops()) {
                routeBuilder.addHops(Hop.newBuilder()
                        .setChanId(hop.channelId().getShortChannelId())
                        .setAmtToForwardMsat(hop.amount().milliSatoshis())
                        .build());
            }
            htlcBuilder.setRoute(routeBuilder.build());
            htlcBuilder.setStatus(HTLCAttempt.HTLCStatus.SUCCEEDED);
            htlcs.add(htlcBuilder.build());
        }
        lnrpc.Payment.Builder builder = lnrpc.Payment.newBuilder()
                .setStatus(status)
                .setPaymentIndex(payment.index())
                .setPaymentHash(payment.paymentHash())
                .setValueMsat(payment.value().milliSatoshis())
                .setFeeMsat(payment.fees().milliSatoshis())
                .setCreationTimeNs(payment.creationDateTime().toInstant(ZoneOffset.UTC).toEpochMilli() * 1_000);
        htlcs.forEach(builder::addHtlcs);
        return builder.build();
    }
}
