package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelIdAndMaxAgeTest {
    @Test
    void channelId() {
        assertThat(new ChannelIdAndMaxAge(CHANNEL_ID_3, Duration.ofSeconds(1)).channelId()).isEqualTo(CHANNEL_ID_3);
    }

    @Test
    void maxAge() {
        Duration maxAge = Duration.ofSeconds(123);
        assertThat(new ChannelIdAndMaxAge(CHANNEL_ID_3, maxAge).maxAge()).isEqualTo(maxAge);
    }
}