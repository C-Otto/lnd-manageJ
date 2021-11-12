package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public  class LocalChannelFixtures {
    public static final Coins LOCAL_BALANCE = Coins.ofSatoshis(1_000);
    public static final Coins RESERVE_LOCAL = Coins.ofSatoshis(100);

    public static final LocalChannel LOCAL_CHANNEL =
            new LocalChannel(CHANNEL, PUBKEY, LOCAL_BALANCE, RESERVE_LOCAL);
    public static final LocalChannel LOCAL_CHANNEL_2 =
            new LocalChannel(CHANNEL_2, PUBKEY, LOCAL_BALANCE, RESERVE_LOCAL);
    public static final LocalChannel LOCAL_CHANNEL_3 =
            new LocalChannel(CHANNEL_3, PUBKEY, LOCAL_BALANCE, RESERVE_LOCAL);
    public static final LocalChannel LOCAL_CHANNEL_TO_NODE_3 =
            new LocalChannel(CHANNEL_TO_NODE_3, PUBKEY, LOCAL_BALANCE, RESERVE_LOCAL);
}
