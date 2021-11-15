package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public  class ClosedChannelFixtures {
    public static final ClosedChannel CLOSED_CHANNEL = new ClosedChannel(CHANNEL, PUBKEY);
    public static final ClosedChannel CLOSED_CHANNEL_2 = new ClosedChannel(CHANNEL_2, PUBKEY);
    public static final ClosedChannel CLOSED_CHANNEL_3 = new ClosedChannel(CHANNEL_3, PUBKEY);
    public static final ClosedChannel CLOSED_CHANNEL_TO_NODE_3 = new ClosedChannel(CHANNEL_TO_NODE_3, PUBKEY);
}
