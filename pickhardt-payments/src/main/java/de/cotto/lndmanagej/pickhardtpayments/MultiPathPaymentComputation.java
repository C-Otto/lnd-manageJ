package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.Routes;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class MultiPathPaymentComputation {
    private final GrpcGetInfo grpcGetInfo;
    private final FlowComputation flowComputation;

    public MultiPathPaymentComputation(GrpcGetInfo grpcGetInfo, FlowComputation flowComputation) {
        this.flowComputation = flowComputation;
        this.grpcGetInfo = grpcGetInfo;
    }

    public MultiPathPayment getMultiPathPaymentTo(Pubkey target, Coins amount) {
        Pubkey source = grpcGetInfo.getPubkey();
        return getMultiPathPayment(source, target, amount);
    }

    public MultiPathPayment getMultiPathPayment(Pubkey source, Pubkey target, Coins amount) {
        Flows flows = flowComputation.getOptimalFlows(source, target, amount);
        if (flows.isEmpty()) {
            return MultiPathPayment.FAILURE;
        }
        double probability = flows.getProbability();
        Set<Route> routes = Routes.fromFlows(source, target, flows);
        Routes.ensureTotalAmount(routes, amount);
        return new MultiPathPayment(amount, probability, routes);
    }
}
