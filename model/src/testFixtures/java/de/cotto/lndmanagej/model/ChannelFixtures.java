package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;

public final class ChannelFixtures {
    public static final Coins CAPACITY = Coins.ofSatoshis(21_000_000L);

    public static final Channel CHANNEL = create(PUBKEY, PUBKEY_2, CHANNEL_ID);
    public static final Channel CHANNEL_2 = create(PUBKEY, PUBKEY_2, CHANNEL_ID_2);
    public static final Channel CHANNEL_3 = create(PUBKEY, PUBKEY_2, CHANNEL_ID_3);
    public static final Channel CHANNEL_TO_NODE_3 = create(PUBKEY, PUBKEY_3, CHANNEL_ID);

    private ChannelFixtures() {
        // do not instantiate
    }

    public static Channel create(Pubkey pubkey1, Pubkey pubkey2, ChannelId channelId) {
        return Channel.builder()
                .withChannelId(channelId)
                .withCapacity(CAPACITY)
                .withNode1(pubkey1)
                .withNode2(pubkey2)
                .build();
    }
}
