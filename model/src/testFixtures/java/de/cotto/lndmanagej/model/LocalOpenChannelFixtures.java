package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.OpenInitiator.REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public  class LocalOpenChannelFixtures {
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL =
            new LocalOpenChannel(CHANNEL_ID, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION, LOCAL);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_2 =
            new LocalOpenChannel(CHANNEL_ID_2, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION, REMOTE);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_3 =
            new LocalOpenChannel(CHANNEL_ID_3, CHANNEL_POINT, CAPACITY, PUBKEY, PUBKEY_2, BALANCE_INFORMATION, LOCAL);
    public static final LocalOpenChannel LOCAL_OPEN_CHANNEL_TO_NODE_3 =
            new LocalOpenChannel(CHANNEL_ID_4, CHANNEL_POINT, CAPACITY_2, PUBKEY, PUBKEY_3, BALANCE_INFORMATION, LOCAL);
}
