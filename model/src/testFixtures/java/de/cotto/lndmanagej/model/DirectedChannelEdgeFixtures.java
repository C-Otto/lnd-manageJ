package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public class DirectedChannelEdgeFixtures {
    public static final DirectedChannelEdge CHANNEL_EDGE_WITH_POLICY = new DirectedChannelEdge(
            CHANNEL_ID,
            CAPACITY,
            PUBKEY,
            PUBKEY_2,
            POLICY_1
    );
}
