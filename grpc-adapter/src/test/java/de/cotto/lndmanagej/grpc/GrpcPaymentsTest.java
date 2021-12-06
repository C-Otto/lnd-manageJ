package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentHop;
import de.cotto.lndmanagej.model.PaymentRoute;
import lnrpc.HTLCAttempt;
import lnrpc.Hop;
import lnrpc.ListPaymentsResponse;
import lnrpc.Payment.PaymentStatus;
import lnrpc.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
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