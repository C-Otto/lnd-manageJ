package de.cotto.lndmanagej.model;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;

public class ResolutionFixtures {
    public static final Resolution RESOLUTION = new Resolution(Optional.of(TRANSACTION_HASH));
    public static final Resolution RESOLUTION_2 = new Resolution(Optional.empty());
}
