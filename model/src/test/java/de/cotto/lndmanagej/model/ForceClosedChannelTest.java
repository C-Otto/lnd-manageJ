package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.CloseType.REMOTE;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_REMOTE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class ForceClosedChannelTest {
    @Test
    void create() {
        assertThat(new ForceClosedChannel(
                CHANNEL_ID,
                CHANNEL_POINT,
                CAPACITY,
                PUBKEY,
                PUBKEY_2,
                TRANSACTION_HASH_2,
                REMOTE
        )).isEqualTo(FORCE_CLOSED_CHANNEL_REMOTE);
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
    void testEquals() {
        EqualsVerifier.forClass(ForceClosedChannel.class).usingGetClass().verify();
    }
}