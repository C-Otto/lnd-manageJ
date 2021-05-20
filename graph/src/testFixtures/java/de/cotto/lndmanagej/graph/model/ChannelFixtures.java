package de.cotto.lndmanagej.graph.model;

import static de.cotto.lndmanagej.graph.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.graph.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.graph.model.NodeFixtures.NODE_2;

public class ChannelFixtures {
    public static final Coins CAPACITY = Coins.ofSatoshis(21_000_000L);

    public static final Channel CHANNEL = Channel.builder()
            .withChannelId(CHANNEL_ID)
            .withCapacity(CAPACITY)
            .withNode1(NODE)
            .withNode2(NODE_2)
            .build();
}
