package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;

public record EdgeWithLiquidityInformation(
        Edge edge,
        Coins availableLiquidityLowerBound,
        Coins availableLiquidityUpperBound
) {
    public EdgeWithLiquidityInformation {
        if (availableLiquidityLowerBound.compareTo(availableLiquidityUpperBound) > 0) {
            throw new IllegalArgumentException("lower bound must not be higher than upper bound");
        }
    }

    public static EdgeWithLiquidityInformation forKnownLiquidity(Edge edge, Coins knownLiquidity) {
        return new EdgeWithLiquidityInformation(edge, knownLiquidity, knownLiquidity);
    }

    public static EdgeWithLiquidityInformation forLowerBound(Edge edge, Coins availableLiquidityLowerBound) {
        return new EdgeWithLiquidityInformation(edge, availableLiquidityLowerBound, edge.capacity());
    }

    public static EdgeWithLiquidityInformation forUpperBound(Edge edge, Coins availableLiquidityUpperBound) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, availableLiquidityUpperBound);
    }

    public static EdgeWithLiquidityInformation forLowerAndUpperBound(
            Edge edge,
            Coins availableLiquidityLowerBound,
            Coins availableLiquidityUpperBound
    ) {
        return new EdgeWithLiquidityInformation(edge, availableLiquidityLowerBound, availableLiquidityUpperBound);
    }

    public ChannelId channelId() {
        return edge.channelId();
    }

    public Pubkey startNode() {
        return edge.startNode();
    }

    public Pubkey endNode() {
        return edge.endNode();
    }

    public Coins capacity() {
        return edge.capacity();
    }

    public Policy policy() {
        return edge.policy();
    }

    public boolean isKnownLiquidity() {
        return availableLiquidityLowerBound.equals(availableLiquidityUpperBound);
    }
}
