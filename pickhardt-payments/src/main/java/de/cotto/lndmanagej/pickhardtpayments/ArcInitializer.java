package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final boolean ignoreFeesForOwnChannels;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ArcInitializer(
            MinCostFlow minCostFlow,
            IntegerMapping<Pubkey> integerMapping,
            IntObjectHashMap<Edge> edgeMapping,
            long quantization,
            int piecewiseLinearApproximations,
            int feeRateWeight,
            Pubkey ownPubkey,
            boolean ignoreFeesForOwnChannels
    ) {
        this.minCostFlow = minCostFlow;
        this.pubkeyToIntegerMapping = integerMapping;
        this.edgeMapping = edgeMapping;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
        this.feeRateWeight = feeRateWeight;
        this.ownPubkey = ownPubkey;
        this.ignoreFeesForOwnChannels = ignoreFeesForOwnChannels;
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

        // always keep one unit as reserve (for fees, or if the amount needs to be rounded up)
        long quantizedLowerBound =
                Math.max(0, quantize(edgeWithLiquidityInformation.availableLiquidityLowerBound()) - 1);
        int remainingPieces = addArcForKnownLiquidity(edge, startNode, endNode, quantizedLowerBound);
        if (remainingPieces == 0) {
            return;
        }

        Coins upperBound = edgeWithLiquidityInformation.availableLiquidityUpperBound();
        long quantizedUpperBound = Math.max(0, quantize(upperBound) - 1);
        long uncertainButPossibleLiquidity = quantizedUpperBound - quantizedLowerBound;
        if (uncertainButPossibleLiquidity <= 0) {
            return;
        }
        remainingPieces = (int) Math.min(remainingPieces, uncertainButPossibleLiquidity);
        long capacityPiece = uncertainButPossibleLiquidity / remainingPieces;
        long unitCost = 10 * quantize(maximumCapacity) / uncertainButPossibleLiquidity;
        long feeRateSummand = feeRateWeight * getFeeRate(edge);
        for (int i = 1; i <= remainingPieces; i++) {
            long cost = i * unitCost + feeRateSummand;
            logger.debug("{} - {}: {} (cost {})", edge.startNode(), edge.endNode(), capacityPiece, cost);
            int arcIndex = minCostFlow.addArcWithCapacityAndUnitCost(
                    startNode,
                    endNode,
                    capacityPiece,
                    cost
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
        logger.debug("{} - {}: {} (cost {})", edge.startNode(), edge.endNode(), quantizedLowerBound, feeRateCost);
        edgeMapping.put(arcIndex, edge);
        return piecewiseLinearApproximations - 1;
    }

    private long getFeeRate(Edge edge) {
        if (ignoreFeesForOwnChannels && ownPubkey.equals(edge.startNode())) {
            return 0;
        }
        long fromBaseFee = (long) Math.ceil(1.0 * 1_000 / quantization * edge.policy().baseFee().milliSatoshis());
        return edge.policy().feeRate() + fromBaseFee;
    }

    private long quantize(Coins coins) {
        return coins.milliSatoshis() / 1_000 / quantization;
    }
}
