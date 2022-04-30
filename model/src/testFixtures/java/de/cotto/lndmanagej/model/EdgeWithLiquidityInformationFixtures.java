package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;

public class EdgeWithLiquidityInformationFixtures {
    public static final EdgeWithLiquidityInformation EDGE_WITH_LIQUIDITY_INFORMATION =
            EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(123));
}
