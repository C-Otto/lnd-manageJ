package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public  class LocalOpenChannelFixtures {
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL =
            new LocalOpenChannel(CHANNEL, PUBKEY, BALANCE_INFORMATION);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_2 =
            new LocalOpenChannel(CHANNEL_2, PUBKEY, BALANCE_INFORMATION);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_3 =
            new LocalOpenChannel(CHANNEL_3, PUBKEY, BALANCE_INFORMATION);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_TO_NODE_3 =
            new LocalOpenChannel(CHANNEL_TO_NODE_3, PUBKEY, BALANCE_INFORMATION);
}
