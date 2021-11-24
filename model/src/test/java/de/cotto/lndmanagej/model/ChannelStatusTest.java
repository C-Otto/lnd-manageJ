package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelStatusTest {
    private final ChannelStatus channelStatus = new ChannelStatus(false, true, false, OPEN);

    @Test
    void privateChannel() {
        assertThat(channelStatus.privateChannel()).isFalse();
    }

    @Test
    void active() {
        assertThat(channelStatus.active()).isTrue();
    }

    @Test
    void closed() {
        assertThat(channelStatus.closed()).isFalse();
    }

    @Test
    void openCloseStatus() {
        assertThat(channelStatus.openCloseStatus()).isEqualTo(OPEN);
    }
}