package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.TRANSACTION_HASH_2;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSE_HEIGHT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.OpenCloseStatus.CLOSED;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class CoopClosedChannelTest {
    @Test
    void create() {
        // CPD-OFF
        assertThat(new CoopClosedChannelBuilder()
                .withChannelId(CHANNEL_ID)
                .withChannelPoint(CHANNEL_POINT)
                .withCapacity(CAPACITY)
                .withOwnPubkey(PUBKEY)
                .withRemotePubkey(PUBKEY_2)
                .withCloseTransactionHash(TRANSACTION_HASH_2)
                .withOpenInitiator(OpenInitiator.LOCAL)
                .withCloseInitiator(CloseInitiator.REMOTE)
                .withCloseHeight(CLOSE_HEIGHT)
                .build()
        ).isEqualTo(CLOSED_CHANNEL);
        // CPD-ON
    }

    @Test
    void closeHeight_zero() {
        ChannelCoreInformation channelCoreInformation = new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY);
        assertThatIllegalArgumentException().isThrownBy(() -> new CoopClosedChannel(
                channelCoreInformation,
                PUBKEY,
                PUBKEY_2,
                TRANSACTION_HASH,
                OpenInitiator.LOCAL,
                CloseInitiator.LOCAL,
                0,
                false
        )).withMessage("Close height must be set");
    }

    @Test
    void getId() {
        assertThat(CLOSED_CHANNEL.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getRemotePubkey() {
        assertThat(CLOSED_CHANNEL.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(CLOSED_CHANNEL.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getTotalSent() {
        assertThat(CLOSED_CHANNEL.getTotalSent()).isEqualTo(Coins.NONE);
    }

    @Test
    void getTotalReceived() {
        assertThat(CLOSED_CHANNEL.getTotalReceived()).isEqualTo(Coins.NONE);
    }

    @Test
    void getChannelPoint() {
        assertThat(CLOSED_CHANNEL.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getPubkeys() {
        assertThat(CLOSED_CHANNEL.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void getCloseTransactionHash() {
        assertThat(CLOSED_CHANNEL.getCloseTransactionHash()).isEqualTo(TRANSACTION_HASH_2);
    }

    @Test
    void getOpenInitiator() {
        assertThat(CLOSED_CHANNEL.getOpenInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void getCloseInitiator() {
        assertThat(CLOSED_CHANNEL.getCloseInitiator()).isEqualTo(CloseInitiator.REMOTE);
    }

    @Test
    void getCloseHeight() {
        assertThat(CLOSED_CHANNEL.getCloseHeight()).isEqualTo(600_000);
    }

    @Test
    void getStatus() {
        assertThat(CLOSED_CHANNEL.getStatus())
                .isEqualTo(new ChannelStatus(false, false, true, CLOSED));
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(CoopClosedChannel.class).usingGetClass().verify();
    }
}