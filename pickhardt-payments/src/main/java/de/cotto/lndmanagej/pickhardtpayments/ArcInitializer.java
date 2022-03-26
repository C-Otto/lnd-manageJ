package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

class ArcInitializer {

    private final MinCostFlow minCostFlow;
    private final IntegerMapping<Pubkey> pubkeyToIntegerMapping;
    private final Map<Integer, Edge> edgeMapping;
    private final long quantization;
    private final int piecewiseLinearApproximations;

    public ArcInitializer(
            MinCostFlow minCostFlow,
            IntegerMapping<Pubkey> integerMapping,
            Map<Integer, Edge> edgeMapping,
            long quantization,
            int piecewiseLinearApproximations
    ) {
        this.minCostFlow = minCostFlow;
        this.pubkeyToIntegerMapping = integerMapping;
        this.edgeMapping = edgeMapping;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
    }

    public void addArcs(Collection<EdgeWithLiquidityInformation> edgesWithLiquidityInformation) {
        Coins maximumCapacity = getMaximumCapacity(edgesWithLiquidityInformation);
        for (EdgeWithLiquidityInformation edgeWithLiquidityInformation : edgesWithLiquidityInformation) {
            addArcs(edgeWithLiquidityInformation, maximumCapacity);
        }
    }

    private void addArcs(EdgeWithLiquidityInformation edgeWithLiquidityInformation, Coins maximumCapacity) {
        long capacitySat = edgeWithLiquidityInformation.availableLiquidityUpperBound().satoshis();
        if (capacitySat < quantization) {
            return;
        }
        Edge edge = edgeWithLiquidityInformation.edge();
        int startNode = pubkeyToIntegerMapping.getMappedInteger(edge.startNode());
        int endNode = pubkeyToIntegerMapping.getMappedInteger(edge.endNode());
        long capacity = capacitySat / quantization;
        long unitCost = maximumCapacity.satoshis() / capacitySat;
        long capacityPiece = capacity / piecewiseLinearApproximations;
        for (int i = 1; i <= piecewiseLinearApproximations; i++) {
            int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(
                    startNode,
                    endNode,
                    capacityPiece,
                    i * unitCost
            );
            edgeMapping.put(arcIndex, edge);
        }
    }

    private Coins getMaximumCapacity(Collection<EdgeWithLiquidityInformation> edgesWithLiquidityInformation) {
        return edgesWithLiquidityInformation.stream()
                .map(EdgeWithLiquidityInformation::edge)
                .map(Edge::capacity)
                .max(Comparator.naturalOrder())
                .orElse(Coins.NONE);
    }
}
