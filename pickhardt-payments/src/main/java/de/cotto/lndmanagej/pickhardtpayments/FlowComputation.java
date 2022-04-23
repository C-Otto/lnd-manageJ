package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.cotto.lndmanagej.pickhardtpayments.PickhardtPaymentsConfiguration.DEFAULT_FEE_RATE_WEIGHT;

@Component
public class FlowComputation {
    private final EdgeComputation edgeComputation;
    private final long quantization;
    private final int piecewiseLinearApproximations;
    private final GrpcGetInfo grpcGetInfo;

    public FlowComputation(
            EdgeComputation edgeComputation,
            GrpcGetInfo grpcGetInfo,
            @Value("${lndmanagej.pickhardtpayments.quantization:10000}") long quantization,
            @Value("${lndmanagej.pickhardtpayments.piecewiseLinearApproximations:5}") int piecewiseLinearApproximations
    ) {
        this.edgeComputation = edgeComputation;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
        this.grpcGetInfo = grpcGetInfo;
    }

    public Flows getOptimalFlows(Pubkey source, Pubkey target, Coins amount) {
        return getOptimalFlows(source, target, amount, DEFAULT_FEE_RATE_WEIGHT);
    }

    public Flows getOptimalFlows(Pubkey source, Pubkey target, Coins amount, int feeRateWeight) {
        MinCostFlowSolver minCostFlowSolver = new MinCostFlowSolver(
                edgeComputation.getEdges(),
                Map.of(source, amount),
                Map.of(target, amount),
                quantization,
                piecewiseLinearApproximations,
                feeRateWeight,
                grpcGetInfo.getPubkey()
        );
        return minCostFlowSolver.solve();
    }

}
