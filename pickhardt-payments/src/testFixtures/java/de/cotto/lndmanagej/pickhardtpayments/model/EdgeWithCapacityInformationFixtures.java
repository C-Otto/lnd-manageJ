package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;

public class EdgeWithCapacityInformationFixtures {
    public static final EdgeWithCapacityInformation EDGE_WITH_CAPACITY_INFORMATION =
            new EdgeWithCapacityInformation(EDGE, Coins.ofSatoshis(123));
}
