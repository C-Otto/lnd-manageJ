package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CoinsAndDurationTest {
    private final CoinsAndDuration coinsAndDuration =
            new CoinsAndDuration(Coins.ofSatoshis(123), Duration.ofMinutes(456));

    @Test
    void coins() {
        assertThat(coinsAndDuration.coins()).isEqualTo(Coins.ofSatoshis(123));
    }

    @Test
    void duration() {
        assertThat(coinsAndDuration.duration()).isEqualTo(Duration.ofMinutes(456));
    }
}
