package de.cotto.lndmanagej.pickhardtpayments.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static org.assertj.core.api.Assertions.assertThat;

class EdgeTest {
    @Test
    void channelId() {
        assertThat(EDGE.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void startNode() {
        assertThat(EDGE.startNode()).isEqualTo(PUBKEY);
    }

    @Test
    void endNode() {
        assertThat(EDGE.endNode()).isEqualTo(PUBKEY_2);
    }

    @Test
    void capacity() {
        assertThat(EDGE.capacity()).isEqualTo(CAPACITY);
    }
}
