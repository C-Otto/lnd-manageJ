package de.cotto.lndmanagej.model;

public record Policy(long feeRate, Coins baseFee, boolean enabled, int timeLockDeltaOfPeer) {
    public static final Policy UNKNOWN = new Policy(0, Coins.NONE, false, 0);

    public boolean disabled() {
        return !enabled;
    }
}
