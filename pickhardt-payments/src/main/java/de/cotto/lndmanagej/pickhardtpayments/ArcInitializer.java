package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;

import java.util.Comparator;
import java.util.Map;

class ArcInitializer {
    // 50 BTC
    private static final Coins ASSUMED_MAXIMUM = Coins.ofSatoshis(5_000_000_000L);

    private final MinCostFlow minCostFlow;
    private final IntegerMapping<Pubkey> pubkeyToIntegerMapping;
    private final Map<Integer, Edge> edgeMapping;
    private final long quantization;
    private final int piecewiseLinearApproximations;
    private final int feeRateWeight;

    public ArcInitializer(
            MinCostFlow minCostFlow,
            IntegerMapping<Pubkey> integerMapping,
            Map<Integer, Edge> edgeMapping,
            long quantization,
            int piecewiseLinearApproximations,
            int feeRateWeight
    ) {
        this.minCostFlow = minCostFlow;
        this.pubkeyToIntegerMapping = integerMapping;
        this.edgeMapping = edgeMapping;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
        this.feeRateWeight = feeRateWeight;
    }

    public void addArcs(EdgesWithLiquidityInformation edgesWithLiquidityInformation) {
        Coins maximumCapacity = getMaximumCapacity(edgesWithLiquidityInformation);
        for (EdgeWithLiquidityInformation edgeWithLiquidityInformation : edgesWithLiquidityInformation.edges()) {
            addArcs(edgeWithLiquidityInformation, maximumCapacity);
        }
    }

    private void addArcs(EdgeWithLiquidityInformation edgeWithLiquidityInformation, Coins maximumCapacity) {
        Edge edge = edgeWithLiquidityInformation.edge();
        int startNode = pubkeyToIntegerMapping.getMappedInteger(edge.startNode());
        int endNode = pubkeyToIntegerMapping.getMappedInteger(edge.endNode());

        long quantizedLowerBound = quantize(edgeWithLiquidityInformation.availableLiquidityLowerBound());
        int remainingPieces = addArcForKnownLiquidity(edge, startNode, endNode, quantizedLowerBound);
        if (remainingPieces == 0) {
            return;
        }

        Coins upperBound = edgeWithLiquidityInformation.availableLiquidityUpperBound();
        long quantizedUpperBound = quantize(upperBound);
        long uncertainButPossibleLiquidity = quantizedUpperBound - quantizedLowerBound;
        long capacityPiece = uncertainButPossibleLiquidity / remainingPieces;
        if (capacityPiece == 0) {
            return;
        }
        long unitCost = 10 * quantize(maximumCapacity) / uncertainButPossibleLiquidity;
        long feeRateSummand = feeRateWeight * edge.policy().feeRate();
        for (int i = 1; i <= remainingPieces; i++) {
            int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(
                    startNode,
                    endNode,
                    capacityPiece,
                    i * unitCost + feeRateSummand
            );
            edgeMapping.put(arcIndex, edge);
        }
    }

    private int addArcForKnownLiquidity(Edge edge, int startNode, int endNode, long quantizedLowerBound) {
        if (quantizedLowerBound <= 0) {
            return piecewiseLinearApproximations;
        }
        long feeRateCost = feeRateWeight * edge.policy().feeRate();
        int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(startNode, endNode, quantizedLowerBound, feeRateCost);
        edgeMapping.put(arcIndex, edge);
        return piecewiseLinearApproximations - 1;
    }

    private Coins getMaximumCapacity(EdgesWithLiquidityInformation edgesWithLiquidityInformation) {
        Coins realMaximum = edgesWithLiquidityInformation.edges().stream()
                .map(EdgeWithLiquidityInformation::edge)
                .map(Edge::capacity)
                .max(Comparator.naturalOrder())
                .orElse(Coins.NONE);
        return realMaximum.maximum(ASSUMED_MAXIMUM);
    }

    private long quantize(Coins coins) {
        return coins.milliSatoshis() / 1_000 / quantization;
    }
}
