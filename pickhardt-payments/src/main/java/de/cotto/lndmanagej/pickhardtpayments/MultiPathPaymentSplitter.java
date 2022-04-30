package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.BasicRoute;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.BasicRoutes;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.Routes;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;

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
        List<BasicRoute> basicRoutes = BasicRoutes.fromFlows(source, target, flows);
        List<Route> routes = getWithLiquidityInformation(basicRoutes);
        List<Route> fixedRoutes = Routes.getFixedWithTotalAmount(routes, amount);
        return new MultiPathPayment(fixedRoutes);
    }

    private List<Route> getWithLiquidityInformation(List<BasicRoute> basicRoutes) {
        return basicRoutes.stream()
                .map(this::getWithLiquidityInformation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Route getWithLiquidityInformation(BasicRoute basicRoute) {
        List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = basicRoute.edges().stream()
                .map(edgeComputation::getEdgeWithLiquidityInformation)
                .toList();
        return new Route(edgesWithLiquidityInformation, basicRoute.amount());
    }
}
