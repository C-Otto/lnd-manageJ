package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import static de.cotto.lndmanagej.model.LiquidityBounds.NO_INFORMATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class LiquidityBoundsWithTimestampTest {

    private static final LiquidityBounds LIQUIDITY_BOUNDS =
            new LiquidityBounds(Coins.ofSatoshis(1), Coins.ofSatoshis(2), Coins.ofSatoshis(3));
    private static final Duration MAX_AGE = Duration.ofHours(1);

    @Test
    void createFromLiquidityBounds() {
        LiquidityBounds expected = new LiquidityBounds(Coins.ofSatoshis(1), null, Coins.NONE);
        LiquidityBoundsWithTimestamp entry = new LiquidityBoundsWithTimestamp(expected);
        assertThat(entry.liquidityBounds()).isEqualTo(expected);
        assertThat(entry.timestamp()).isAfter(Instant.now().minus(10, ChronoUnit.SECONDS));
    }

    @Test
    void isTooOld_for_max_age_old_entry() {
        LiquidityBoundsWithTimestamp entry = new LiquidityBoundsWithTimestamp(
                LIQUIDITY_BOUNDS,
                Instant.now().minus(1, ChronoUnit.HOURS)
        );
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(
                () -> entry.isTooOld(MAX_AGE)
        );
    }

    @Test
    void isTooOld_for_not_yet_max_age_old_entry() {
        LiquidityBoundsWithTimestamp entry = new LiquidityBoundsWithTimestamp(
                LIQUIDITY_BOUNDS,
                Instant.now().minus(59, ChronoUnit.MINUTES)
        );
        assertThat(entry.isTooOld(MAX_AGE)).isFalse();
    }

    @Test
    void liquidityBounds() {
        LiquidityBounds liquidityBounds = LIQUIDITY_BOUNDS;
        LiquidityBoundsWithTimestamp entry = new LiquidityBoundsWithTimestamp(liquidityBounds, Instant.now());
        assertThat(entry.liquidityBounds()).isEqualTo(liquidityBounds);
    }

    @Test
    void timestamp() {
        Instant timestamp = Instant.now().minus(59, ChronoUnit.MINUTES);
        LiquidityBoundsWithTimestamp entry = new LiquidityBoundsWithTimestamp(NO_INFORMATION, timestamp);
        assertThat(entry.timestamp()).isEqualTo(timestamp);
    }
}
