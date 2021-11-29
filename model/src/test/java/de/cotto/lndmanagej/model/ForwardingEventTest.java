package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ForwardingEventFixtures.FORWARDING_EVENT;
import static org.assertj.core.api.Assertions.assertThat;

class ForwardingEventTest {
    @Test
    void offset() {
        assertThat(FORWARDING_EVENT.index()).isEqualTo(1);
    }

    @Test
    void amountIn() {
        assertThat(FORWARDING_EVENT.amountIn()).isEqualTo(Coins.ofMilliSatoshis(1_000));
    }

    @Test
    void amountOut() {
        assertThat(FORWARDING_EVENT.amountOut()).isEqualTo(Coins.ofMilliSatoshis(900));
    }

    @Test
    void channelIn() {
        assertThat(FORWARDING_EVENT.channelIn()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void channelOut() {
        assertThat(FORWARDING_EVENT.channelOut()).isEqualTo(CHANNEL_ID_2);
    }

    @Test
    void timestamp() {
        assertThat(FORWARDING_EVENT.timestamp()).isEqualTo(
                LocalDateTime.of(2021, 11, 29, 18, 30, 0)
        );
    }
}