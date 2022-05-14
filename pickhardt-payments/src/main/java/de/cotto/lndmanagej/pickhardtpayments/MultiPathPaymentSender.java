package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import org.springframework.stereotype.Component;

@Component
public class MultiPathPaymentSender {
    private final GrpcPayments grpcPayments;
    private final PaymentLoop paymentLoop;

    public MultiPathPaymentSender(GrpcPayments grpcPayments, PaymentLoop paymentLoop) {
        this.grpcPayments = grpcPayments;
        this.paymentLoop = paymentLoop;
    }

    public PaymentStatus payPaymentRequest(String paymentRequest, int feeRateWeight) {
        DecodedPaymentRequest decodedPaymentRequest = grpcPayments.decodePaymentRequest(paymentRequest).orElse(null);
        if (decodedPaymentRequest == null) {
            return PaymentStatus.UNABLE_TO_DECODE_PAYMENT_REQUEST;
        }
        PaymentStatus paymentStatus = new PaymentStatus(decodedPaymentRequest.paymentHash());
        paymentLoop.start(decodedPaymentRequest, feeRateWeight, paymentStatus);
        return paymentStatus;
    }
}
