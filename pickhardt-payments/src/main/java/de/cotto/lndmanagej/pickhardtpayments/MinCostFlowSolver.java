package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.Loader;
import com.google.ortools.graph.MinCostFlow;
import com.google.ortools.graph.MinCostFlowBase;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static com.google.ortools.graph.MinCostFlowBase.Status.OPTIMAL;

class MinCostFlowSolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MinCostFlow minCostFlow = new MinCostFlow();
    private final IntegerMapping<Pubkey> integerMapping = new IntegerMapping<>();
    private final Map<Integer, Edge> edgeMapping = new LinkedHashMap<>();
    private final long quantization;

    static {
        Loader.loadNativeLibraries();
    }

    public MinCostFlowSolver(
            EdgesWithLiquidityInformation edgesWithLiquidityInformation,
            Map<Pubkey, Coins> sources,
            Map<Pubkey, Coins> sinks,
            long quantization,
            int piecewiseLinearApproximations,
            int feeRateWeight
    ) {
        this.quantization = quantization;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                piecewiseLinearApproximations,
                feeRateWeight
        );
        arcInitializer.addArcs(edgesWithLiquidityInformation);
        setSupply(sources, sinks);
    }

    public Flows solve() {
        MinCostFlowBase.Status status = minCostFlow.solve();
        if (status != OPTIMAL) {
            logger.warn("Solving the min cost flow problem failed. Solver status: {}", status);
            return new Flows();
        }
        Flows flows = new Flows();
        for (int i = 0; i < minCostFlow.getNumArcs(); i++) {
            long flowAmount = minCostFlow.getFlow(i);
            if (flowAmount == 0) {
                continue;
            }
            Edge edge = Objects.requireNonNull(edgeMapping.get(i));
            Flow flow = new Flow(edge, Coins.ofSatoshis(flowAmount * quantization));
            flows.add(flow);
        }
        return flows;
    }

    private void setSupply(Map<Pubkey, Coins> sources, Map<Pubkey, Coins> sinks) {
        Coins totalSourceAmount = sources.values().stream().reduce(Coins::add).orElse(Coins.NONE);
        Coins totalSinkAmount = sinks.values().stream().reduce(Coins::add).orElse(Coins.NONE);
        if (!totalSourceAmount.equals(totalSinkAmount)) {
            throw new IllegalArgumentException(
                    "Source and sink amounts are different, got " + totalSourceAmount + " and " + totalSinkAmount
            );
        }
        for (Map.Entry<Pubkey, Coins> entry : sources.entrySet()) {
            Pubkey node = entry.getKey();
            long supply = entry.getValue().satoshis() / quantization;
            minCostFlow.setNodeSupply(integerMapping.getMappedInteger(node), supply);
        }
        for (Map.Entry<Pubkey, Coins> entry : sinks.entrySet()) {
            Pubkey node = entry.getKey();
            long supply = -entry.getValue().satoshis() / quantization;
            minCostFlow.setNodeSupply(integerMapping.getMappedInteger(node), supply);
        }
    }
}
