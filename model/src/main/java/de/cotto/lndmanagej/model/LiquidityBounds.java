package de.cotto.lndmanagej.model;

import com.google.common.annotations.VisibleForTesting;

import javax.annotation.CheckForNull;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class LiquidityBounds {
    private static final Duration DEFAULT_MAX_AGE = Duration.of(1, ChronoUnit.HOURS);
    private final Duration maxAge;
    private Instant lowerBoundLastUpdate;
    private Instant upperBoundLastUpdate;

    private Coins lowerBound;
    @CheckForNull
    private Coins upperBound;
    private Coins inFlight;

    public LiquidityBounds() {
        this(DEFAULT_MAX_AGE);
    }

    public LiquidityBounds(Duration maxAge) {
        lowerBound = Coins.NONE;
        inFlight = Coins.NONE;
        lowerBoundLastUpdate = Instant.now();
        upperBoundLastUpdate = Instant.now();
        this.maxAge = maxAge;
    }

    public void move(Coins amount) {
        lowerBound = lowerBound.subtract(amount).maximum(Coins.NONE);
    }

    @SuppressWarnings("PMD.NullAssignment")
    public void available(Coins amount) {
        resetOldLowerBound();
        lowerBoundLastUpdate = Instant.now();
        lowerBound = lowerBound.maximum(amount);
        if (upperBound != null && lowerBound.compareTo(upperBound) >= 0) {
            upperBound = null;
        }
    }

    public void unavailable(Coins amount) {
        resetOldUpperBound();
        upperBoundLastUpdate = Instant.now();
        Coins newUpperBound = amount.subtract(Coins.ofSatoshis(1));
        if (upperBound == null) {
            upperBound = newUpperBound;
        } else {
            upperBound = upperBound.minimum(newUpperBound);
        }
        lowerBound = lowerBound.minimum(upperBound);
    }

    public void addAsInFlight(Coins amount) {
        inFlight = Coins.NONE.maximum(inFlight.add(amount));
    }

    public Coins getLowerBound() {
        resetOldLowerBound();
        return Coins.NONE.maximum(lowerBound.subtract(inFlight));
    }

    public Optional<Coins> getUpperBound() {
        resetOldUpperBound();
        return Optional.ofNullable(upperBound).map(upperBound -> Coins.NONE.maximum(upperBound.subtract(inFlight)));
    }

    @SuppressWarnings("PMD.NullAssignment")
    private void resetOldUpperBound() {
        if (upperBoundLastUpdate.isBefore(Instant.now().minus(maxAge))) {
            upperBound = null;
        }
    }

    private void resetOldLowerBound() {
        if (lowerBoundLastUpdate.isBefore(Instant.now().minus(maxAge))) {
            lowerBound = Coins.NONE;
        }
    }

    @VisibleForTesting
    void setLowerBoundLastUpdate(Instant lowerBoundLastUpdate) {
        this.lowerBoundLastUpdate = lowerBoundLastUpdate;
    }

    @VisibleForTesting
    void setUpperBoundLastUpdate(Instant upperBoundLastUpdate) {
        this.upperBoundLastUpdate = upperBoundLastUpdate;
    }
}
