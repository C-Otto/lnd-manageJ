package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ChannelTest {

    private static final Channel CHANNEL = new TestableChannel(CHANNEL_ID, CAPACITY, CHANNEL_POINT, PUBKEY_2, PUBKEY);

    @Test
    void identical_pubkeys() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new TestableChannel(CHANNEL_ID, CAPACITY, CHANNEL_POINT, PUBKEY, PUBKEY)
        ).withMessage("Pubkeys must not be the same");
    }

    @Test
    void getId() {
        assertThat(CHANNEL.getId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void getNodes() {
        assertThat(CHANNEL.getPubkeys()).containsExactlyInAnyOrder(PUBKEY, PUBKEY_2);
    }

    @Test
    void getCapacity() {
        assertThat(CHANNEL.getCapacity()).isEqualTo(CAPACITY);
    }

    @Test
    void getChannelPoint() {
        assertThat(CHANNEL.getChannelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Channel.class).usingGetClass().verify();
    }

    @Test
    void testEquals_reversed_nodes() {
        Channel channel2 = new TestableChannel(CHANNEL_ID, CAPACITY, CHANNEL_POINT, PUBKEY, PUBKEY_2);
        assertThat(CHANNEL).isEqualTo(channel2);
    }

    private static class TestableChannel extends Channel {
        public TestableChannel(
                ChannelId channelId,
                Coins capacity,
                ChannelPoint channelPoint,
                Pubkey pubkey1,
                Pubkey pubkey2
        ) {
            super(channelId, channelPoint, Coins.ofMilliSatoshis(capacity.milliSatoshis()), pubkey1, pubkey2);
        }
    }
}