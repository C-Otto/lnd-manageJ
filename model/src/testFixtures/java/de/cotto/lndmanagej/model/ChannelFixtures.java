package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;

public final class ChannelFixtures {
    public static final Coins CAPACITY = Coins.ofSatoshis(21_000_000L);

    public static final Channel CHANNEL = create(NODE, NODE_2, CHANNEL_ID);
    public static final Channel CHANNEL_2 = create(NODE, NODE_2, CHANNEL_ID_2);
    public static final Channel CHANNEL_3 = create(NODE, NODE_2, CHANNEL_ID_3);

    private ChannelFixtures() {
        // do not instantiate
    }

    public static Channel create(Node node1, Node node2, ChannelId channelId) {
        return Channel.builder()
                .withChannelId(channelId)
                .withCapacity(CAPACITY)
                .withNode1(node1)
                .withNode2(node2)
                .build();
    }
}
