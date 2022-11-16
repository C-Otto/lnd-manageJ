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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
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
import static lnrpc.HTLCAttempt.HTLCStatus.FAILED;
import static lnrpc.Payment.PaymentStatus.IN_FLIGHT;
import static lnrpc.Payment.PaymentStatus.SUCCEEDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

    @Nested
    class GetCompletePaymentsAfter {
        @Test
        void empty_optional() {
            when(grpcService.getPayments(anyLong(), anyInt(), anyBoolean())).thenReturn(Optional.empty());
            assertThat(grpcPayments.getCompletePaymentsAfter(0L)).isEmpty();
        }

        @Test
        void no_payment() {
            mockResponse();
            assertThat(grpcPayments.getCompletePaymentsAfter(0L)).contains(List.of());
        }

        @Test
        void with_payments() {
            mockResponse(payment(SUCCEEDED, PAYMENT), payment(SUCCEEDED, PAYMENT_2));
            assertThat(grpcPayments.getCompletePaymentsAfter(0L)).contains(
                    List.of(PAYMENT, PAYMENT_2)
            );
        }

        @Test
        void skips_unsuccessful_htlcs() {
            Route route = Route.newBuilder().addHops(Hop.getDefaultInstance()).build();
            HTLCAttempt htlc = HTLCAttempt.newBuilder()
                    .setRoute(route)
                    .setStatus(HTLCAttempt.HTLCStatus.IN_FLIGHT)
                    .build();
            lnrpc.Payment payment = lnrpc.Payment.newBuilder()
                    .setStatus(SUCCEEDED)
                    .addHtlcs(htlc)
                    .build();

            mockResponse(payment);
            assertThat(grpcPayments.getCompletePaymentsAfter(0L).orElseThrow().stream().map(Payment::routes))
                    .contains(List.of());
        }

        @Test
        void payment_without_channel_id() {
            mockPaymentWithHop(0L);
            assertThat(grpcPayments.getCompletePaymentsAfter(0L).orElseThrow().get(0).routes()).hasSize(1);
        }

        @Test
        void payment_with_very_large_channel_id() {
            ChannelId channelId = ChannelId.fromCompactForm("16735713:1045061:30574");
            mockPaymentWithHop(channelId.getShortChannelId());
            List<PaymentRoute> routes = grpcPayments.getCompletePaymentsAfter(0L).orElseThrow().get(0).routes();
            assertThat(routes).hasSize(1);
            assertThat(routes.get(0).firstHop().orElseThrow().channelId()).isEqualTo(channelId);
        }

        @Test
        void ignores_non_settled_payment() {
            mockResponse(payment(IN_FLIGHT, null));
            assertThat(grpcPayments.getCompletePaymentsAfter(0L)).contains(List.of());
        }

        @Test
        void ignores_failed_payment() {
            mockResponse(payment(PaymentStatus.FAILED, null));
            assertThat(grpcPayments.getCompletePaymentsAfter(0L)).contains(List.of());
        }

        @Test
        void starts_at_the_beginning() {
            grpcPayments.getCompletePaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(eq(ADD_INDEX_OFFSET), anyInt(), anyBoolean());
        }

        @Test
        void uses_limit() {
            grpcPayments.getCompletePaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(eq(ADD_INDEX_OFFSET), eq(LIMIT), anyBoolean());
        }

        @Test
        void only_requests_settled_payment() {
            grpcPayments.getCompletePaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(anyLong(), anyInt(), eq(false));
        }

        private void mockPaymentWithHop(long channelId) {
            Hop hop = Hop.newBuilder()
                    .setChanId(channelId)
                    .setAmtToForwardMsat(123L)
                    .build();
            Route route = Route.newBuilder().addHops(hop).build();
            HTLCAttempt htlc = HTLCAttempt.newBuilder()
                    .setStatus(HTLCAttempt.HTLCStatus.SUCCEEDED)
                    .setRoute(route)
                    .build();
            lnrpc.Payment payment = lnrpc.Payment.newBuilder()
                    .setStatus(SUCCEEDED)
                    .setPaymentIndex(PAYMENT.index())
                    .setPaymentHash(PAYMENT.paymentHash())
                    .setValueMsat(PAYMENT.value().milliSatoshis())
                    .setFeeMsat(PAYMENT.fees().milliSatoshis())
                    .setCreationTimeNs(PAYMENT.creationDateTime().toInstant(ZoneOffset.UTC).toEpochMilli() * 1_000)
                    .addHtlcs(htlc)
                    .build();
            mockResponse(payment);
        }
    }

    @Nested
    class GetCompleteAndPendingPaymentsAfter {
        @Test
        void empty_optional() {
            when(grpcService.getPayments(anyLong(), anyInt(), anyBoolean())).thenReturn(Optional.empty());
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).isEmpty();
        }

        @Test
        void no_payment() {
            mockResponse();
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(List.of());
        }

        @Test
        void with_payments() {
            mockResponse(payment(SUCCEEDED, PAYMENT), payment(SUCCEEDED, PAYMENT_2));
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(
                    List.of(Optional.of(PAYMENT), Optional.of(PAYMENT_2))
            );
        }

        @Test
        void skips_failed_payment() {
            mockResponse(payment(PaymentStatus.FAILED, PAYMENT), payment(SUCCEEDED, PAYMENT_2));
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(
                    List.of(Optional.of(PAYMENT_2))
            );
        }

        @Test
        void with_in_flight_payment() {
            mockResponse(payment(IN_FLIGHT, PAYMENT), payment(SUCCEEDED, PAYMENT_2));
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(
                    List.of(Optional.empty(), Optional.of(PAYMENT_2))
            );
        }

        @Test
        void with_recent_in_flight_payment_without_pending_htlc() {
            Payment payment = paymentWithAge(Duration.ofHours(23));
            mockResponse(payment(IN_FLIGHT, payment, FAILED));
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(
                    List.of(Optional.empty())
            );
        }

        @Test
        void skips_old_in_flight_payment_without_pending_htlc() {
            Payment payment = paymentWithAge(Duration.ofHours(24));
            mockResponse(payment(IN_FLIGHT, payment, FAILED));
            assertThat(grpcPayments.getCompleteAndPendingPaymentsAfter(0L)).contains(List.of());
        }

        @Test
        void starts_at_the_beginning() {
            grpcPayments.getCompleteAndPendingPaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(eq(ADD_INDEX_OFFSET), anyInt(), anyBoolean());
        }

        @Test
        void uses_limit() {
            grpcPayments.getCompleteAndPendingPaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(eq(ADD_INDEX_OFFSET), eq(LIMIT), anyBoolean());
        }

        @Test
        void also_requests_non_settled_payment() {
            grpcPayments.getCompleteAndPendingPaymentsAfter(ADD_INDEX_OFFSET);
            verify(grpcService).getPayments(anyLong(), anyInt(), eq(true));
        }
    }

    private static Payment paymentWithAge(Duration age) {
        return new Payment(
                PAYMENT.index(),
                PAYMENT.paymentHash(),
                LocalDateTime.now(ZoneOffset.UTC).minus(age),
                PAYMENT.value(),
                PAYMENT.fees(),
                PAYMENT.routes()
        );
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

    private void mockResponse(lnrpc.Payment... payments) {
        ListPaymentsResponse.Builder builder = ListPaymentsResponse.newBuilder();
        Arrays.stream(payments).forEach(builder::addPayments);
        ListPaymentsResponse response = builder.build();
        when(grpcService.getPayments(anyLong(), anyInt(), anyBoolean())).thenReturn(Optional.of(response));
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

    private lnrpc.Payment payment(PaymentStatus paymentStatus, @Nullable Payment payment) {
        return payment(paymentStatus, payment, HTLCAttempt.HTLCStatus.SUCCEEDED);
    }

    private lnrpc.Payment payment(
            PaymentStatus paymentStatus,
            @Nullable Payment payment,
            HTLCAttempt.HTLCStatus htlcStatus
    ) {
        if (payment == null) {
            return lnrpc.Payment.newBuilder().setStatus(paymentStatus).build();
        }
        HTLCAttempt.Builder htlcBuilder = HTLCAttempt.newBuilder();
        List<HTLCAttempt> htlcs = new ArrayList<>();
        for (PaymentRoute paymentRoute : payment.routes()) {
            Route.Builder routeBuilder = Route.newBuilder();
            paymentRoute.firstHop().ifPresent(hop -> addHop(routeBuilder, hop));
            paymentRoute.lastHop().ifPresent(hop -> addHop(routeBuilder, hop));
            htlcBuilder.setRoute(routeBuilder.build());
            htlcBuilder.setStatus(htlcStatus);
            htlcs.add(htlcBuilder.build());
        }
        lnrpc.Payment.Builder builder = lnrpc.Payment.newBuilder()
                .setStatus(paymentStatus)
                .setPaymentIndex(payment.index())
                .setPaymentHash(payment.paymentHash())
                .setValueMsat(payment.value().milliSatoshis())
                .setFeeMsat(payment.fees().milliSatoshis())
                .setCreationTimeNs(payment.creationDateTime().toInstant(ZoneOffset.UTC).toEpochMilli() * 1_000_000);
        htlcs.forEach(builder::addHtlcs);
        return builder.build();
    }

    private static void addHop(Route.Builder routeBuilder, PaymentHop hop) {
        routeBuilder.addHops(Hop.newBuilder()
                .setChanId(hop.channelId().getShortChannelId())
                .setAmtToForwardMsat(hop.amount().milliSatoshis())
                .build());
    }
}
