package de.cotto.lndmanagej.model;

import java.time.Instant;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class MissionControlEntryFixtures {
    public static final MissionControlEntry FAILURE =
            new MissionControlEntry(PUBKEY, PUBKEY_2, Coins.ofSatoshis(123), Instant.now(), true);
    public static final MissionControlEntry SUCCESS =
            new MissionControlEntry(PUBKEY, PUBKEY_2, Coins.ofSatoshis(123), Instant.now(), false);
}
