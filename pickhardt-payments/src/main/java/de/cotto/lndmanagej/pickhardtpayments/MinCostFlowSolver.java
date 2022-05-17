package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.Loader;
import com.google.ortools.graph.MinCostFlow;
import com.google.ortools.graph.MinCostFlowBase;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static com.google.ortools.graph.MinCostFlowBase.Status.OPTIMAL;

@SuppressWarnings({"PMD.AvoidCatchingNPE", "PMD.AvoidCatchingGenericException"})
class MinCostFlowSolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Nullable
    private final MinCostFlow minCostFlow;
    private final IntegerMapping<Pubkey> integerMapping = new IntegerMapping<>();
    private final IntObjectHashMap<Edge> edgeMapping;
    private static final boolean UNABLE_TO_LOAD_LIBRARY;

    private final long quantization;

    static {
        boolean loaded;
        try {
            Loader.loadNativeLibraries();
            loaded = true;
        } catch (NullPointerException nullPointerException) {
            loaded = false;
        }
        UNABLE_TO_LOAD_LIBRARY = !loaded;
    }

    public MinCostFlowSolver(
            EdgesWithLiquidityInformation edgesWithLiquidityInformation,
            Map<Pubkey, Coins> sources,
            Map<Pubkey, Coins> sinks,
            long quantization,
            int piecewiseLinearApproximations,
            int feeRateWeight,
            Pubkey ownPubkey
    ) {
        this.quantization = quantization;
        int initialMapCapacity = edgesWithLiquidityInformation.edges().size() * (piecewiseLinearApproximations - 1);
        edgeMapping = new IntObjectHashMap<>(initialMapCapacity);
        if (UNABLE_TO_LOAD_LIBRARY) {
            logger.error("Unable to initialize OR library, see https://github.com/C-Otto/lnd-manageJ/issues/13");
            minCostFlow = null;
            return;
        }
        minCostFlow = new MinCostFlow();
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                piecewiseLinearApproximations,
                feeRateWeight,
                ownPubkey
        );
        arcInitializer.addArcs(edgesWithLiquidityInformation);
        setSupply(sources, sinks);
    }

    public Flows solve() {
        if (minCostFlow == null) {
            return new Flows();
        }
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
        if (flows.isEmpty()) {
            logger.warn("Solver returned result with no flows: {}", flows);
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
            Objects.requireNonNull(minCostFlow).setNodeSupply(integerMapping.getMappedInteger(node), supply);
        }
        for (Map.Entry<Pubkey, Coins> entry : sinks.entrySet()) {
            Pubkey node = entry.getKey();
            long supply = -entry.getValue().satoshis() / quantization;
            Objects.requireNonNull(minCostFlow).setNodeSupply(integerMapping.getMappedInteger(node), supply);
        }
    }
}
