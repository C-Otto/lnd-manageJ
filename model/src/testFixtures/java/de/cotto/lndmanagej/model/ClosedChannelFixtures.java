package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.UnresolvedClosedChannelFixtures.UNRESOLVED_CLOSED_CHANNEL_3;

public  class ClosedChannelFixtures {
    public static final ClosedChannel CLOSED_CHANNEL = ClosedChannel.create(UNRESOLVED_CLOSED_CHANNEL);
    public static final ClosedChannel CLOSED_CHANNEL_2 = ClosedChannel.create(UNRESOLVED_CLOSED_CHANNEL_2);
    public static final ClosedChannel CLOSED_CHANNEL_3 = ClosedChannel.create(UNRESOLVED_CLOSED_CHANNEL_3);
}
