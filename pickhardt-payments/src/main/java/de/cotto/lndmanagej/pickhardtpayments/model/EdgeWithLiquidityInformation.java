package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

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

    public boolean isKnownLiquidity() {
        return availableLiquidityLowerBound.equals(availableLiquidityUpperBound);
    }
}
