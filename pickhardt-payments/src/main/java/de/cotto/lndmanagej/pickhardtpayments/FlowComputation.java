package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import org.springframework.stereotype.Component;

import java.util.Map;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.PIECEWISE_LINEAR_APPROXIMATIONS;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.QUANTIZATION;

@Component
public class FlowComputation {
    @SuppressWarnings("PMD.LongVariable")
    private static final int DEFAULT_PIECEWISE_LINEAR_APPROXIMATIONS = 5;
    private static final int DEFAULT_QUANTIZATION = 10_000;

    private final EdgeComputation edgeComputation;
    private final GrpcGetInfo grpcGetInfo;
    private final ConfigurationService configurationService;

    public FlowComputation(
            EdgeComputation edgeComputation,
            GrpcGetInfo grpcGetInfo,
            ConfigurationService configurationService
    ) {
        this.edgeComputation = edgeComputation;
        this.grpcGetInfo = grpcGetInfo;
        this.configurationService = configurationService;
    }

    public Flows getOptimalFlows(Pubkey source, Pubkey target, Coins amount, int feeRateWeight) {
        int quantization = getQuantization(amount);
        int piecewiseLinearApproximations = configurationService.getIntegerValue(PIECEWISE_LINEAR_APPROXIMATIONS)
                .orElse(DEFAULT_PIECEWISE_LINEAR_APPROXIMATIONS);
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

    private int getQuantization(Coins amount) {
        int quantization = configurationService.getIntegerValue(QUANTIZATION)
                .orElse(DEFAULT_QUANTIZATION);
        return (int) Math.min(amount.satoshis(), quantization);
    }

}
