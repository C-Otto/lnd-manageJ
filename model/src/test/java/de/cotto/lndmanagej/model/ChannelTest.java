package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ChannelTest {
    @Test
    void builder_without_arguments() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder().build()
        );
    }

    @Test
    void builder_without_channelId() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder()
                        .withCapacity(CAPACITY)
                        .withChannelPoint(CHANNEL_POINT)
                        .withNode1(PUBKEY)
                        .withNode2(PUBKEY_2)
                        .build()
        );
    }

    @Test
    void builder_without_capacity() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder()
                        .withChannelId(CHANNEL_ID)
                        .withNode1(PUBKEY)
                        .withChannelPoint(CHANNEL_POINT)
                        .withNode2(PUBKEY_2)
                        .build()
        );
    }

    @Test
    void builder_without_channel_point() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder()
                        .withChannelId(CHANNEL_ID)
                        .withNode1(PUBKEY)
                        .withCapacity(CAPACITY)
                        .withNode2(PUBKEY_2)
                        .build()
        );
    }

    @Test
    void builder_without_node1() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder()
                        .withChannelId(CHANNEL_ID)
                        .withCapacity(CAPACITY)
                        .withChannelPoint(CHANNEL_POINT)
                        .withNode2(PUBKEY_2)
                        .build()
        );
    }

    @Test
    void builder_without_node2() {
        assertThatNullPointerException().isThrownBy(
                () -> Channel.builder()
                        .withChannelId(CHANNEL_ID)
                        .withCapacity(CAPACITY)
                        .withChannelPoint(CHANNEL_POINT)
                        .withNode1(PUBKEY)
                        .build()
        );
    }

    @Test
    void builder_identical_pubkeys() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> Channel.builder()
                        .withChannelId(CHANNEL_ID)
                        .withCapacity(CAPACITY)
                        .withChannelPoint(CHANNEL_POINT)
                        .withNode1(PUBKEY)
                        .withNode2(PUBKEY)
                        .build()
        ).withMessage("Pubkeys must not be the same");
    }

    @Test
    void builder_with_all_arguments() {
        Channel channel = Channel.builder()
                .withChannelId(CHANNEL_ID)
                .withCapacity(CAPACITY)
                .withNode1(PUBKEY)
                .withNode2(PUBKEY_2)
                .withChannelPoint(CHANNEL_POINT)
                .build();
        assertThat(channel).isEqualTo(CHANNEL);
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
    void getWithId() {
        assertThat(CHANNEL.getWithId(CHANNEL_ID_2)).isEqualTo(CHANNEL_2);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Channel.class).usingGetClass().verify();
    }

    @Test
    void testEquals_reversed_nodes() {
        Channel channel = Channel.builder()
                .withChannelId(CHANNEL_ID)
                .withCapacity(CAPACITY)
                .withChannelPoint(CHANNEL_POINT)
                .withNode1(PUBKEY_2)
                .withNode2(PUBKEY)
                .build();
        assertThat(CHANNEL).isEqualTo(channel);
    }
}