package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
            verify(grpcSendToRoute).sendToRoute(route, DECODED_PAYMENT_REQUEST);
        }
    }
}
