package de.cotto.lndmanagej.model;

import java.time.Instant;
import java.time.temporal.TemporalAmount;

public record LiquidityBoundsWithTimestamp(LiquidityBounds liquidityBounds, Instant timestamp) {
    public LiquidityBoundsWithTimestamp(LiquidityBounds liquidityBounds) {
        this(liquidityBounds, Instant.now());
    }

    public boolean isTooOld(TemporalAmount maxAge) {
        return timestamp.isBefore(Instant.now().minus(maxAge));
    }
}
