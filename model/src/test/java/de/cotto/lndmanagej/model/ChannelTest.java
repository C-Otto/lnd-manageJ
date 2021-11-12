package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CHANNEL;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
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
                        .withNode1(PUBKEY)
                        .build()
        );
    }

    @Test
    void builder_with_all_arguments() {
        Channel channel = Channel.builder()
                .withChannelId(CHANNEL_ID)
                .withCapacity(CAPACITY)
                .withNode1(PUBKEY)
                .withNode2(PUBKEY_2)
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
    void testEquals() {
        EqualsVerifier.forClass(Channel.class).usingGetClass().verify();
    }

    @Test
    void testEquals_reversed_nodes() {
        Channel channel = Channel.builder()
                .withChannelId(CHANNEL_ID)
                .withCapacity(CAPACITY)
                .withNode1(PUBKEY_2)
                .withNode2(PUBKEY)
                .build();
        assertThat(CHANNEL).isEqualTo(channel);
    }

    @Test
    void testToString() {
        assertThat(CHANNEL).hasToString(
                "Channel[" +
                        "channelId=" + CHANNEL_ID +
                        ", capacity=" + CAPACITY +
                        ", pubkeys=[" + PUBKEY + ", " + PUBKEY_2 + "]" +
                        "]"
        );
    }
}