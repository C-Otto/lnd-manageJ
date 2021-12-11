package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSE_HEIGHT;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_REMOTE;
import static de.cotto.lndmanagej.model.OpenCloseStatus.CLOSED;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.ResolutionFixtures.COMMIT_CLAIMED;
import static de.cotto.lndmanagej.model.ResolutionFixtures.INCOMING_HTLC_CLAIMED;
import static org.assertj.core.api.Assertions.assertThat;

class ForceClosedChannelTest {
    @Test
    void create() {
        assertThat(new ForceClosedChannelBuilder()
                .withChannelId(CHANNEL_ID)
                .withChannelPoint(CHANNEL_POINT)
                .withCapacity(CAPACITY)
                .withOwnPubkey(PUBKEY)
                .withRemotePubkey(PUBKEY_2)
                .withCloseTransactionHash(TRANSACTION_HASH_2)
                .withOpenInitiator(OpenInitiator.LOCAL)
                .withCloseInitiator(CloseInitiator.REMOTE)
                .withCloseHeight(CLOSE_HEIGHT)
                .withResolutions(Set.of(INCOMING_HTLC_CLAIMED, COMMIT_CLAIMED))
                .build()
        ).isEqualTo(FORCE_CLOSED_CHANNEL);
    }

    @Test
    void getId() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getRemotePubkey() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getTotalSent() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getTotalSent()).isEqualTo(Coins.NONE);
    }

    @Test
    void getTotalReceived() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getTotalReceived()).isEqualTo(Coins.NONE);
    }

    @Test
    void getChannelPoint() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getPubkeys() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void getCloseTransactionHash() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getCloseTransactionHash()).isEqualTo(TRANSACTION_HASH_2);
    }

    @Test
    void getOpenInitiator() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getOpenInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void getCloseInitiator() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getCloseInitiator()).isEqualTo(CloseInitiator.REMOTE);
    }

    @Test
    void isForceClosed() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.isForceClosed()).isTrue();
    }

    @Test
    void getAsForceClosedChannel() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getAsForceClosedChannel()).isEqualTo(FORCE_CLOSED_CHANNEL_REMOTE);
    }

    @Test
    void getCloseHeight() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getCloseHeight()).isEqualTo(987_654);
    }

    @Test
    void getStatus() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getStatus())
                .isEqualTo(new ChannelStatus(false, false, true, CLOSED));
    }

    @Test
    void isClosed() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.isClosed()).isTrue();
    }

    @Test
    void getAsClosedChannel() {
        assertThat(FORCE_CLOSED_CHANNEL_REMOTE.getAsClosedChannel()).isEqualTo(FORCE_CLOSED_CHANNEL_REMOTE);
    }

    @Test
    void getResolutions() {
        assertThat(FORCE_CLOSED_CHANNEL.getResolutions())
                .containsExactlyInAnyOrder(INCOMING_HTLC_CLAIMED, COMMIT_CLAIMED);
    }

    @Test
    void isBreach() {
        assertThat(FORCE_CLOSED_CHANNEL.isBreach()).isFalse();
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ForceClosedChannel.class).usingGetClass().verify();
    }
}