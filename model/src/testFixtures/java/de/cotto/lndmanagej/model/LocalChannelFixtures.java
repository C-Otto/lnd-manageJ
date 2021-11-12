package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public  class LocalChannelFixtures {
    public static final LocalChannel LOCAL_CHANNEL = new LocalChannel(CHANNEL, PUBKEY, BALANCE_INFORMATION);
    public static final LocalChannel LOCAL_CHANNEL_2 = new LocalChannel(CHANNEL_2, PUBKEY, BALANCE_INFORMATION);
    public static final LocalChannel LOCAL_CHANNEL_3 = new LocalChannel(CHANNEL_3, PUBKEY, BALANCE_INFORMATION);
    public static final LocalChannel LOCAL_CHANNEL_TO_NODE_3 =
            new LocalChannel(CHANNEL_TO_NODE_3, PUBKEY, BALANCE_INFORMATION);
}
