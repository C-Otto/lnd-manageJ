package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT_2;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.OpenInitiator.REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public  class WaitingCloseChannelFixtures {
    public static final WaitingCloseChannel WAITING_CLOSE_CHANNEL = new WaitingCloseChannel(
            new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
            PUBKEY,
            PUBKEY_2,
            LOCAL
    );
    public static final WaitingCloseChannel WAITING_CLOSE_CHANNEL_2 = new WaitingCloseChannel(
            new ChannelCoreInformation(CHANNEL_ID_2, CHANNEL_POINT_2, CAPACITY),
            PUBKEY,
            PUBKEY_2,
            REMOTE
    );
    public static final WaitingCloseChannel WAITING_CLOSE_CHANNEL_TO_NODE_3 = new WaitingCloseChannel(
            new ChannelCoreInformation(CHANNEL_ID_3, CHANNEL_POINT, CAPACITY),
            PUBKEY,
            PUBKEY_3,
            LOCAL
    );
}
