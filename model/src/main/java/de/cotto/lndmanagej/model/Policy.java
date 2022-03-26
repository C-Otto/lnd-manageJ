package de.cotto.lndmanagej.model;

public record Policy(long feeRate, Coins baseFee, boolean enabled) {
    public static final Policy UNKNOWN = new Policy(0, Coins.NONE, false);

    public boolean disabled() {
        return !enabled;
    }
}
