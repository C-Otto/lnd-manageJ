package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithCapacityInformation;
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

    public void addArcs(Collection<EdgeWithCapacityInformation> edgesWithCapacityInformation) {
        Coins maximumCapacity = getMaximumCapacity(edgesWithCapacityInformation);
        for (EdgeWithCapacityInformation edgeWithCapacityInformation : edgesWithCapacityInformation) {
            addArcs(edgeWithCapacityInformation, maximumCapacity);
        }
    }

    private void addArcs(EdgeWithCapacityInformation edgeWithCapacityInformation, Coins maximumCapacity) {
        long capacitySat = edgeWithCapacityInformation.availableCapacity().satoshis();
        if (capacitySat < quantization) {
            return;
        }
        int startNode = pubkeyToIntegerMapping.getMappedInteger(edgeWithCapacityInformation.edge().startNode());
        int endNode = pubkeyToIntegerMapping.getMappedInteger(edgeWithCapacityInformation.edge().endNode());
        long capacity = capacitySat / quantization;
        long unitCost = maximumCapacity.satoshis() / quantization / capacity;
        long capacityPiece = capacity / piecewiseLinearApproximations;
        for (int i = 1; i <= piecewiseLinearApproximations; i++) {
            int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(
                    startNode,
                    endNode,
                    capacityPiece,
                    i * unitCost
            );
            edgeMapping.put(arcIndex, edgeWithCapacityInformation.edge());
        }
    }

    private Coins getMaximumCapacity(Collection<EdgeWithCapacityInformation> edgesWithCapacityInformation) {
        return edgesWithCapacityInformation.stream()
                .map(EdgeWithCapacityInformation::availableCapacity)
                .max(Comparator.naturalOrder())
                .orElse(Coins.NONE);
    }
}
