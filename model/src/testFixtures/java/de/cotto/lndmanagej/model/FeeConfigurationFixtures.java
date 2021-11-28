package de.cotto.lndmanagej.model;

public class FeeConfigurationFixtures {
    public static final FeeConfiguration FEE_CONFIGURATION =
            new FeeConfiguration(
                    1,
                    Coins.ofMilliSatoshis(2),
                    3,
                    Coins.ofMilliSatoshis(4),
                    false,
                    true
            );
}
