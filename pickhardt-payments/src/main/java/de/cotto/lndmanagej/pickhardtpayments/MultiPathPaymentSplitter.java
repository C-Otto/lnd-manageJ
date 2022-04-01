package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.Routes;
import org.springframework.stereotype.Component;

import java.util.Set;

import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;
import static java.util.stream.Collectors.toSet;

@Component
public class MultiPathPaymentSplitter {
    private final GrpcGetInfo grpcGetInfo;
    private final FlowComputation flowComputation;
    private final EdgeComputation edgeComputation;

    public MultiPathPaymentSplitter(
            GrpcGetInfo grpcGetInfo,
            FlowComputation flowComputation,
            EdgeComputation edgeComputation
    ) {
        this.flowComputation = flowComputation;
        this.grpcGetInfo = grpcGetInfo;
        this.edgeComputation = edgeComputation;
    }

    public MultiPathPayment getMultiPathPaymentTo(Pubkey target, Coins amount) {
        Pubkey source = grpcGetInfo.getPubkey();
        return getMultiPathPayment(source, target, amount);
    }

    public MultiPathPayment getMultiPathPaymentTo(Pubkey target, Coins amount, int feeRateWeight) {
        Pubkey source = grpcGetInfo.getPubkey();
        return getMultiPathPayment(source, target, amount, feeRateWeight);
    }

    public MultiPathPayment getMultiPathPayment(Pubkey source, Pubkey target, Coins amount) {
        return getMultiPathPayment(source, target, amount, DEFAULT_FEE_RATE_WEIGHT);
    }

    public MultiPathPayment getMultiPathPayment(Pubkey source, Pubkey target, Coins amount, int feeRateWeight) {
        Flows flows = flowComputation.getOptimalFlows(source, target, amount, feeRateWeight);
        if (flows.isEmpty()) {
            return MultiPathPayment.FAILURE;
        }
        Set<Route> routes = Routes.fromFlows(source, target, flows);
        Set<Route> routesWithLiquidityInformation = getWithLiquidityInformation(routes);
        Routes.ensureTotalAmount(routesWithLiquidityInformation, amount);
        return new MultiPathPayment(routesWithLiquidityInformation);
    }

    private Set<Route> getWithLiquidityInformation(Set<Route> routes) {
        return routes.stream().map(this::getWithLiquidityInformation).collect(toSet());
    }

    private Route getWithLiquidityInformation(Route route) {
        Set<EdgeWithLiquidityInformation> liquidityInformation = route.edges().stream()
                .map(edgeComputation::getEdgeWithLiquidityInformation)
                .collect(toSet());
        return route.withLiquidityInformation(liquidityInformation);
    }
}
