package de.cotto.lndmanagej.model;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Locale;

public record Coins(long milliSatoshis) implements Comparable<Coins> {
    private static final int SCALE = 3;
    public static final Coins NONE = new Coins(0);

    public static Coins ofSatoshis(long satoshis) {
        return ofMilliSatoshis(satoshis * 1_000);
    }

    public static Coins ofMilliSatoshis(long milliSatoshis) {
        if (milliSatoshis == 0) {
            return NONE;
        }
        return new Coins(milliSatoshis);
    }

    public long satoshis() {
        if (milliSatoshis % 1_000 != 0) {
            throw new IllegalStateException();
        }
        return milliSatoshis / 1_000;
    }

    public Coins add(Coins summand) {
        if (summand.milliSatoshis == 0) {
            return this;
        }
        if (this.milliSatoshis == 0) {
            return summand;
        }
        return ofMilliSatoshis(milliSatoshis + summand.milliSatoshis);
    }

    public Coins subtract(Coins subtrahend) {
        if (subtrahend.milliSatoshis == 0) {
            return this;
        }
        return ofMilliSatoshis(milliSatoshis - subtrahend.milliSatoshis);
    }

    public Coins absolute() {
        return ofMilliSatoshis(Math.abs(milliSatoshis));
    }

    @Override
    public int compareTo(Coins other) {
        return Long.compare(milliSatoshis, other.milliSatoshis);
    }

    public Coins minimum(@Nullable Coins other) {
        if (other == null || compareTo(other) <= 0) {
            return this;
        }
        return other;
    }

    public Coins maximum(@Nullable Coins other) {
        if (other == null || compareTo(other) >= 0) {
            return this;
        }
        return other;
    }

    public Coins negate() {
        return ofMilliSatoshis(-milliSatoshis);
    }

    public boolean isPositive() {
        return compareTo(NONE) > 0;
    }

    public boolean isNegative() {
        return compareTo(NONE) < 0;
    }

    public boolean isNonPositive() {
        return !isPositive();
    }

    public boolean isNonNegative() {
        return !isNegative();
    }

    public double getMillionSatoshis() {
        return 1.0 * milliSatoshis / 1_000 / 1_000_000;
    }

    @Override
    public String toString() {
        double coins = BigDecimal.valueOf(milliSatoshis, SCALE).doubleValue();
        return String.format(Locale.ENGLISH, "%,.3f", coins);
    }

    public String toStringSat() {
        long coins = BigDecimal.valueOf(milliSatoshis(), SCALE).longValue();
        return String.format(Locale.ENGLISH, "%,d", coins);
    }
}
