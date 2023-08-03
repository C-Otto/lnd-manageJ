package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.pickhardtpayments.model.InstantWithString;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.RouteHintService;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.ReactiveStreamReader.readAll;
import static de.cotto.lndmanagej.ReactiveStreamReader.readMessages;
import static de.cotto.lndmanagej.model.DecodedPaymentRequestFixtures.DECODED_PAYMENT_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentSenderTest {
    @InjectMocks
    private MultiPathPaymentSender multiPathPaymentSender;

    @Mock
    private GrpcPayments grpcPayments;

    @Mock
    private PaymentLoop paymentLoop;

    @Mock
    private RouteHintService routeHintService;

    @Test
    void unable_to_decode_payment_request() {
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest("foo", feeRateWeight(123));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isFailure()).isTrue();
        softly.assertThat(readAll(paymentStatus)).map(InstantWithString::string)
                .contains("Unable to decode payment request");
        softly.assertAll();
        verifyNoInteractions(paymentLoop);
    }

    @Test
    void fees_decoded_payment_request_to_route_hint_service() {
        when(grpcPayments.decodePaymentRequest(DECODED_PAYMENT_REQUEST.paymentRequest()))
                .thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));

        multiPathPaymentSender.payPaymentRequest(DECODED_PAYMENT_REQUEST.paymentRequest(), feeRateWeight(0));
        verify(routeHintService).addDecodedPaymentRequest(DECODED_PAYMENT_REQUEST);
    }

    @Test
    void returns_initialized_payment_status() {
        when(grpcPayments.decodePaymentRequest(DECODED_PAYMENT_REQUEST.paymentRequest()))
                .thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));

        int feeRateWeight = 123;
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest(
                DECODED_PAYMENT_REQUEST.paymentRequest(),
                feeRateWeight(feeRateWeight)
        );
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paymentStatus.isSuccess()).isFalse();
        softly.assertThat(paymentStatus.isFailure()).isFalse();
        softly.assertThat(readMessages(paymentStatus, 1)).map(InstantWithString::string)
                .contains("Initializing payment " + DECODED_PAYMENT_REQUEST.paymentHash());
        softly.assertAll();
    }

    @Test
    void starts_payment_loop() {
        when(grpcPayments.decodePaymentRequest(DECODED_PAYMENT_REQUEST.paymentRequest()))
                .thenReturn(Optional.of(DECODED_PAYMENT_REQUEST));

        PaymentOptions paymentOptions = feeRateWeight(123);
        PaymentStatus paymentStatus = multiPathPaymentSender.payPaymentRequest(
                DECODED_PAYMENT_REQUEST.paymentRequest(),
                paymentOptions
        );
        verify(paymentLoop).start(DECODED_PAYMENT_REQUEST, paymentOptions, paymentStatus);
        assertThat(readMessages(paymentStatus, 2)).map(InstantWithString::string)
                .contains("Payment Options: " + paymentOptions);
    }

    private PaymentOptions feeRateWeight(int feeRateWeight) {
        return PaymentOptions.forFeeRateWeight(feeRateWeight);
    }
}
