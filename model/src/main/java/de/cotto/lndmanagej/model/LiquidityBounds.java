package de.cotto.lndmanagej.model;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class LiquidityBounds {
    private final Coins lowerBound;
    @CheckForNull
    private final Coins upperBound;
    private final Coins inFlight;

    public static final LiquidityBounds NO_INFORMATION = new LiquidityBounds();

    @SuppressWarnings("PMD.NullAssignment")
    public LiquidityBounds() {
        this.lowerBound = Coins.NONE;
        this.upperBound = null;
        this.inFlight = Coins.NONE;
    }

    public LiquidityBounds(Coins lowerBound, @Nullable Coins upperBound, Coins inFlight) {
        throwIfNegative(lowerBound, "invalid lower bound: ");
        throwIfNegative(upperBound, "invalid upper bound: ");
        throwIfNegative(inFlight, "invalid in flight amount: ");
        if (upperBound != null && lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException(
                    "lower bound must not be above upper bound: " + lowerBound + " <! " + upperBound
            );
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.inFlight = inFlight;
    }

    public Optional<LiquidityBounds> withMovedCoins(Coins amount) {
        Coins newLowerBound = lowerBound.subtract(amount).maximum(Coins.NONE);
        return create(newLowerBound, upperBound, inFlight);
    }

    @SuppressWarnings("PMD.NullAssignment")
    public Optional<LiquidityBounds> withAvailableCoins(Coins amount) {
        Coins newLowerBound = lowerBound.maximum(amount);
        Coins newUpperBound;
        if (upperBound != null && newLowerBound.compareTo(upperBound) > 0) {
            newUpperBound = null;
        } else {
            newUpperBound = upperBound;
        }
        return create(newLowerBound, newUpperBound, inFlight);
    }

    public Optional<LiquidityBounds> withUnavailableCoins(Coins amount) {
        Coins oneSatBelowUnavailableAmount = amount.subtract(Coins.ofSatoshis(1));
        Coins newUpperBound = oneSatBelowUnavailableAmount.add(inFlight).minimum(upperBound).maximum(Coins.NONE);
        Coins newLowerBound = lowerBound.minimum(newUpperBound.subtract(inFlight)).maximum(Coins.NONE);
        return create(newLowerBound, newUpperBound, inFlight);
    }

    public Optional<LiquidityBounds> withAdditionalInFlight(Coins amount) {
        Coins newInFlight = inFlight.add(amount).maximum(Coins.NONE);
        return create(lowerBound, upperBound, newInFlight);
    }

    public Coins getLowerBound() {
        return Coins.NONE.maximum(lowerBound.subtract(inFlight));
    }

    public Optional<Coins> getUpperBound() {
        return Optional.ofNullable(upperBound);
    }

    private Optional<LiquidityBounds> create(
            Coins newLowerBound,
            @Nullable Coins newUpperBound,
            Coins newInFlight
    ) {
        if (newLowerBound.equals(lowerBound)
                && Objects.equals(newUpperBound, upperBound)
                && newInFlight.equals(inFlight)
        ) {
            return Optional.empty();
        }
        if (Coins.NONE.equals(newLowerBound) && newUpperBound == null && Coins.NONE.equals(newInFlight)) {
            return Optional.of(NO_INFORMATION);
        }
        return Optional.of(new LiquidityBounds(newLowerBound, newUpperBound, newInFlight));
    }

    private void throwIfNegative(@Nullable Coins coins, String prefix) {
        if (coins != null && coins.isNegative()) {
            throw new IllegalArgumentException(prefix + coins);
        }
    }
}
