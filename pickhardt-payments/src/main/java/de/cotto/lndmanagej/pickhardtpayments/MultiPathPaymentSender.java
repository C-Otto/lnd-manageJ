package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentStatus;
import de.cotto.lndmanagej.service.RouteHintService;
import org.springframework.stereotype.Component;

@Component
public class MultiPathPaymentSender {
    private final GrpcPayments grpcPayments;
    private final PaymentLoop paymentLoop;
    private final RouteHintService routeHintService;

    public MultiPathPaymentSender(
            GrpcPayments grpcPayments,
            PaymentLoop paymentLoop,
            RouteHintService routeHintService
    ) {
        this.grpcPayments = grpcPayments;
        this.paymentLoop = paymentLoop;
        this.routeHintService = routeHintService;
    }

    public PaymentStatus payPaymentRequest(String paymentRequest, int feeRateWeight) {
        DecodedPaymentRequest decodedPaymentRequest = grpcPayments.decodePaymentRequest(paymentRequest).orElse(null);
        if (decodedPaymentRequest == null) {
            return PaymentStatus.UNABLE_TO_DECODE_PAYMENT_REQUEST;
        }
        routeHintService.addDecodedPaymentRequest(decodedPaymentRequest);

        PaymentStatus paymentStatus = new PaymentStatus(decodedPaymentRequest.paymentHash());
        paymentLoop.start(decodedPaymentRequest, feeRateWeight, paymentStatus);
        return paymentStatus;
    }
}
