package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.DirectedChannelEdgeFixtures.CHANNEL_EDGE_WITH_POLICY;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class DirectedChannelEdgeTest {
    @Test
    void channelId() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void capacity() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.capacity()).isEqualTo(CAPACITY);
    }

    @Test
    void source() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.source()).isEqualTo(PUBKEY);
    }

    @Test
    void target() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.target()).isEqualTo(PUBKEY_2);
    }

    @Test
    void policy() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.policy()).isEqualTo(POLICY_1);
    }

    @Test
    void reversePolicy() {
        assertThat(CHANNEL_EDGE_WITH_POLICY.reversePolicy()).isEqualTo(POLICY_2);
    }
}
