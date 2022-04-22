package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class PendingOpenChannelFixtures {

    public static final PendingOpenChannel PENDING_OPEN_CHANNEL =
            new PendingOpenChannel(
                    CHANNEL_POINT,
                    CAPACITY,
                    PUBKEY,
                    PUBKEY_2,
                    LOCAL,
                    false
            );
}
