package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcPayments;
import de.cotto.lndmanagej.grpc.GrpcSendToRoute;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DecodedPaymentRequest;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MultiPathPaymentSender {
    private final GrpcPayments grpcPayments;
    private final GrpcSendToRoute grpcSendToRoute;
    private final MultiPathPaymentSplitter multiPathPaymentSplitter;
    private final MultiPathPaymentObserver multiPathPaymentObserver;

    public MultiPathPaymentSender(
            GrpcPayments grpcPayments,
            GrpcSendToRoute grpcSendToRoute,
            MultiPathPaymentSplitter multiPathPaymentSplitter,
            MultiPathPaymentObserver multiPathPaymentObserver
    ) {
        this.grpcPayments = grpcPayments;
        this.grpcSendToRoute = grpcSendToRoute;
        this.multiPathPaymentSplitter = multiPathPaymentSplitter;
        this.multiPathPaymentObserver = multiPathPaymentObserver;
    }

    public MultiPathPayment payPaymentRequest(String paymentRequest, int feeRateWeight) {
        DecodedPaymentRequest decodedPaymentRequest = grpcPayments.decodePaymentRequest(paymentRequest).orElse(null);
        if (decodedPaymentRequest == null) {
            return MultiPathPayment.FAILURE;
        }
        Pubkey destination = decodedPaymentRequest.destination();
        Coins amount = decodedPaymentRequest.amount();
        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPaymentTo(destination, amount, feeRateWeight);
        List<Route> routes = multiPathPayment.routes();
        for (Route route : routes) {
            grpcSendToRoute.sendToRoute(route, decodedPaymentRequest, multiPathPaymentObserver.forRoute(route));
        }
        return multiPathPayment;
    }
}
