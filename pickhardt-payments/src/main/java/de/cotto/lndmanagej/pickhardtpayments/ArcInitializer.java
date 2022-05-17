package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;

class ArcInitializer {
    // 50 BTC
    private static final Coins ASSUMED_MAXIMUM = Coins.ofSatoshis(5_000_000_000L);

    private final MinCostFlow minCostFlow;
    private final IntegerMapping<Pubkey> pubkeyToIntegerMapping;
    private final IntObjectHashMap<Edge> edgeMapping;
    private final long quantization;
    private final int piecewiseLinearApproximations;
    private final int feeRateWeight;
    private final Pubkey ownPubkey;

    public ArcInitializer(
            MinCostFlow minCostFlow,
            IntegerMapping<Pubkey> integerMapping,
            IntObjectHashMap<Edge> edgeMapping,
            long quantization,
            int piecewiseLinearApproximations,
            int feeRateWeight,
            Pubkey ownPubkey
    ) {
        this.minCostFlow = minCostFlow;
        this.pubkeyToIntegerMapping = integerMapping;
        this.edgeMapping = edgeMapping;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
        this.feeRateWeight = feeRateWeight;
        this.ownPubkey = ownPubkey;
    }

    public void addArcs(EdgesWithLiquidityInformation edgesWithLiquidityInformation) {
        Coins maximumCapacity = edgesWithLiquidityInformation.maximumCapacity().maximum(ASSUMED_MAXIMUM);
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
        long feeRateSummand = feeRateWeight * getFeeRate(edge);
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
        long feeRateCost = feeRateWeight * getFeeRate(edge);
        int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(startNode, endNode, quantizedLowerBound, feeRateCost);
        edgeMapping.put(arcIndex, edge);
        return piecewiseLinearApproximations - 1;
    }

    private long getFeeRate(Edge edge) {
        if (ownPubkey.equals(edge.startNode())) {
            return 0;
        }
        long fromBaseFee = (long) Math.ceil(1.0 * 1_000 / quantization * edge.policy().baseFee().milliSatoshis());
        return edge.policy().feeRate() + fromBaseFee;
    }

    private long quantize(Coins coins) {
        return coins.milliSatoshis() / 1_000 / quantization;
    }
}
