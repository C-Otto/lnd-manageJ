package de.cotto.lndmanagej.model;

import java.math.BigDecimal;
import java.util.Locale;

public class Coins implements Comparable<Coins> {
    private static final int SCALE = 3;
    public static final Coins NONE = Coins.ofSatoshis(0);

    private final long milliSatoshis;

    protected Coins(long milliSatoshis) {
        this.milliSatoshis = milliSatoshis;
    }

    public static Coins ofSatoshis(long satoshis) {
        return new Coins(satoshis * 1_000);
    }

    public static Coins ofMilliSatoshis(long milliSatoshis) {
        return new Coins(milliSatoshis);
    }

    public long getSatoshis() {
        if (milliSatoshis % 1_000 != 0) {
            throw new IllegalStateException();
        }
        return milliSatoshis / 1_000;
    }

    public long getMilliSatoshis() {
        return milliSatoshis;
    }

    public Coins add(Coins summand) {
        return Coins.ofMilliSatoshis(milliSatoshis + summand.milliSatoshis);
    }

    public Coins subtract(Coins subtrahend) {
        return Coins.ofMilliSatoshis(milliSatoshis - subtrahend.milliSatoshis);
    }

    public Coins absolute() {
        return Coins.ofMilliSatoshis(Math.abs(milliSatoshis));
    }

    @Override
    public int compareTo(Coins other) {
        return Long.compare(milliSatoshis, other.milliSatoshis);
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

    @Override
    public String toString() {
        double coins = BigDecimal.valueOf(milliSatoshis, SCALE).doubleValue();
        return String.format(Locale.ENGLISH, "%,.3f", coins);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Coins coins = (Coins) other;

        return milliSatoshis == coins.milliSatoshis;
    }

    @Override
    public int hashCode() {
        return (int) (milliSatoshis ^ (milliSatoshis >>> 32));
    }
}
