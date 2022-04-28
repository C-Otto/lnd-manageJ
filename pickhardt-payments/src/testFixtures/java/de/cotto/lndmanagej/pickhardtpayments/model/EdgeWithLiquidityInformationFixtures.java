package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;

public class EdgeWithLiquidityInformationFixtures {
    public static final EdgeWithLiquidityInformation EDGE_WITH_LIQUIDITY_INFORMATION =
            EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(123));
}
