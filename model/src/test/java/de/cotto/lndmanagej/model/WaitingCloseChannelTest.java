package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.OpenCloseStatus.WAITING_CLOSE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class WaitingCloseChannelTest {
    @Test
    void create() {
        assertThat(new WaitingCloseChannel(
                new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                OpenInitiator.LOCAL)
        ).isEqualTo(WAITING_CLOSE_CHANNEL);
    }

    @Test
    void getId() {
        assertThat(WAITING_CLOSE_CHANNEL.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getRemotePubkey() {
        assertThat(WAITING_CLOSE_CHANNEL.getRemotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(WAITING_CLOSE_CHANNEL.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getTotalSent() {
        assertThat(WAITING_CLOSE_CHANNEL.getTotalSent()).isEqualTo(Coins.NONE);
    }

    @Test
    void getTotalReceived() {
        assertThat(WAITING_CLOSE_CHANNEL.getTotalReceived()).isEqualTo(Coins.NONE);
    }

    @Test
    void getChannelPoint() {
        assertThat(WAITING_CLOSE_CHANNEL.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void getPubkeys() {
        assertThat(WAITING_CLOSE_CHANNEL.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void getStatus() {
        assertThat(WAITING_CLOSE_CHANNEL.getStatus())
                .isEqualTo(new ChannelStatus(false, false, false, WAITING_CLOSE));
    }

    @Test
    void isClosed() {
        assertThat(WAITING_CLOSE_CHANNEL.isClosed()).isFalse();
    }

    @Test
    void getAsClosedChannel() {
        assertThatIllegalStateException()
                .isThrownBy(WAITING_CLOSE_CHANNEL::getAsClosedChannel)
                .withMessage("Channel is not closed");
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(WaitingCloseChannel.class).usingGetClass().verify();
    }

    @Test
    void getOpenInitiator() {
        assertThat(WAITING_CLOSE_CHANNEL.getOpenInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }
}