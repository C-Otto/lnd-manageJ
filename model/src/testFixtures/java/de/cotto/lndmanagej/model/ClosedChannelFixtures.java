package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;

public final class ClosedChannelFixtures {
    private ClosedChannelFixtures() {
        // do not instantiate
    }

    public static <T extends ClosedChannel> ClosedChannelBuilder<T> getWithDefaults(ClosedChannelBuilder<T> builder) {
        return builder.withChannelId(CHANNEL_ID)
                .withChannelPoint(CHANNEL_POINT)
                .withCapacity(CAPACITY)
                .withOwnPubkey(PUBKEY)
                .withRemotePubkey(PUBKEY_2)
                .withCloseTransactionHash(TRANSACTION_HASH_2)
                .withOpenInitiator(OpenInitiator.LOCAL)
                .withCloseInitiator(CloseInitiator.REMOTE);
    }
}
