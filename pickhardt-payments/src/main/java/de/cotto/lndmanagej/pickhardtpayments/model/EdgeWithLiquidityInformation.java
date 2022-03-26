package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

public record EdgeWithLiquidityInformation(
        Edge edge,
        Coins availableLiquidityLowerBound,
        Coins availableLiquidityUpperBound
) {
    public static EdgeWithLiquidityInformation forKnownLiquidity(Edge edge, Coins knownLiquidity) {
        return new EdgeWithLiquidityInformation(edge, knownLiquidity, knownLiquidity);
    }

    public static EdgeWithLiquidityInformation forUpperBound(Edge edge, Coins availableLiquidityUpperBound) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, availableLiquidityUpperBound);
    }
}
