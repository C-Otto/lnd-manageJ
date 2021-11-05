package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;

public class ChannelFixtures {
    public static final Coins CAPACITY = Coins.ofSatoshis(21_000_000L);

    public static final Channel CHANNEL = Channel.builder()
            .withChannelId(ChannelIdFixtures.CHANNEL_ID)
            .withCapacity(CAPACITY)
            .withNode1(NODE)
            .withNode2(NODE_2)
            .build();
}
