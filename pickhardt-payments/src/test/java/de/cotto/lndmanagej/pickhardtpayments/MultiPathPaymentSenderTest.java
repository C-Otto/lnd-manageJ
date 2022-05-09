package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.grpc.SendToRouteObserver;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentSenderTest {
    private static final int FEE_RATE_WEIGHT = 213;
    private static final String PAYMENT_REQUEST = "abc";

    @InjectMocks
    private MultiPathPaymentSender multiPathPaymentSender;

    @Mock
    private GrpcPayments grpcPayments;

    @Mock
    private GrpcSendToRoute grpcSendToRoute;

    @Mock
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private MultiPathPaymentObserver multiPathPaymentObserver;

    @Test
    void payment_request_cannot_be_decoded() {
        when(grpcPayments.decodePaymentRequest(any())).thenReturn(Optional.empty());
        MultiPathPayment multiPathPayment = multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, FEE_RATE_WEIGHT);
        assertThat(multiPathPayment.isFailure()).isTrue();
        verifyNoInteractions(grpcSendToRoute);
    }

    @Test
    void failure_from_splitter() {
        when(grpcPayments.decodePaymentRequest(PAYMENT_REQUEST)).thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), anyInt()))
                .thenReturn(MultiPathPayment.FAILURE);
        MultiPathPayment multiPathPayment = multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, FEE_RATE_WEIGHT);
        assertThat(multiPathPayment.isFailure()).isTrue();
        verifyNoInteractions(grpcSendToRoute);
    }

    @Test
    void sends_for_each_route() {
        when(grpcPayments.decodePaymentRequest(PAYMENT_REQUEST)).thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(
                DECODED_PAYMENT_REQUEST.destination(),
                DECODED_PAYMENT_REQUEST.amount(),
                FEE_RATE_WEIGHT)
        ).thenReturn(MULTI_PATH_PAYMENT);
        MultiPathPayment multiPathPayment = multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, FEE_RATE_WEIGHT);
        assertThat(multiPathPayment.isFailure()).isFalse();
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            verify(grpcSendToRoute).sendToRoute(eq(route), eq(DECODED_PAYMENT_REQUEST), any());
        }
    }

    @Test
    void registers_observers_for_routes() {
        when(grpcPayments.decodePaymentRequest(any())).thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));
        when(multiPathPaymentSplitter.getMultiPathPaymentTo(any(), any(), anyInt())).thenReturn(MULTI_PATH_PAYMENT);
        Map<Route, SendToRouteObserver> expected = new LinkedHashMap<>();
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            SendToRouteObserver expectedObserver = mock(SendToRouteObserver.class);
            when(multiPathPaymentObserver.forRoute(route)).thenReturn(expectedObserver);
            expected.put(route, expectedObserver);
        }
        multiPathPaymentSender.payPaymentRequest(PAYMENT_REQUEST, FEE_RATE_WEIGHT);
        for (Route route : MULTI_PATH_PAYMENT.routes()) {
            verify(grpcSendToRoute).sendToRoute(route, DECODED_PAYMENT_REQUEST, requireNonNull(expected.get(route)));
        }
    }
}
