package de.cotto.lndmanagej.model;

public record Policy(
        long feeRate,
        Coins baseFee,
        long inboundFeeRate,
        Coins inboundBaseFee,
        boolean enabled,
        int timeLockDelta,
        Coins minHtlc,
        Coins maxHtlc
) {
    public static final Policy UNKNOWN = new Policy(
            0,
            Coins.NONE,
            0,
            Coins.NONE,
            false,
            0,
            Coins.NONE,
            Coins.NONE
    );

    public Policy(long feeRate, Coins baseFee, boolean enabled, int timeLockDelta, Coins minHtlc, Coins maxHtlc) {
        this(feeRate, baseFee, 0, Coins.NONE, enabled, timeLockDelta, minHtlc, maxHtlc);
    }

    public boolean disabled() {
        return !enabled;
    }
}
