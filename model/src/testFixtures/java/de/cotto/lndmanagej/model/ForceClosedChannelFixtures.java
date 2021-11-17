package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.CloseType.BREACH;
import static de.cotto.lndmanagej.model.CloseType.LOCAL;
import static de.cotto.lndmanagej.model.CloseType.REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class ForceClosedChannelFixtures {
    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_REMOTE =
            new ForceClosedChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2, REMOTE);
    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_LOCAL =
            new ForceClosedChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2, LOCAL);
    public static final ForceClosedChannel FORCE_CLOSED_CHANNEL_BREACH =
            new ForceClosedChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, TRANSACTION_HASH_2, BREACH);
}
