package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_3;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_UNRESOLVED_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;

public  class UnresolvedClosedChannelFixtures {
    public static final UnresolvedClosedChannel UNRESOLVED_CLOSED_CHANNEL =
            new UnresolvedClosedChannel(CHANNEL, PUBKEY);
    public static final UnresolvedClosedChannel CLOSED_CHANNEL_UNRESOLVED_ID =
            new UnresolvedClosedChannel(CHANNEL_UNRESOLVED_ID, PUBKEY);
    public static final UnresolvedClosedChannel UNRESOLVED_CLOSED_CHANNEL_2 =
            new UnresolvedClosedChannel(CHANNEL_2, PUBKEY);
    public static final UnresolvedClosedChannel UNRESOLVED_CLOSED_CHANNEL_3 =
            new UnresolvedClosedChannel(CHANNEL_3, PUBKEY);
}
