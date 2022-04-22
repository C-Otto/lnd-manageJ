package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PendingOpenChannelFixtures.PENDING_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class PendingOpenChannelTest {
    @Test
    void getChannelPoint() {
        assertThat(PENDING_OPEN_CHANNEL.channelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getCapacity() {
        assertThat(PENDING_OPEN_CHANNEL.capacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getOwnPubkey() {
        assertThat(PENDING_OPEN_CHANNEL.ownPubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void getRemotePubkey() {
        assertThat(PENDING_OPEN_CHANNEL.remotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getOpenInitiator() {
        assertThat(PENDING_OPEN_CHANNEL.openInitiator()).isEqualTo(LOCAL);
    }

    @Test
    void isPrivateChannel() {
        assertThat(PENDING_OPEN_CHANNEL.isPrivate()).isFalse();
    }
}