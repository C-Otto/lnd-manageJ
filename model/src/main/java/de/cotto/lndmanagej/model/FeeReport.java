package de.cotto.lndmanagej.model;

public record FeeReport(Coins earned, Coins sourced) {
    public static final FeeReport EMPTY = new FeeReport(Coins.NONE, Coins.NONE);

    public FeeReport add(FeeReport other) {
        return new FeeReport(earned.add(other.earned), sourced.add(other.sourced));
    }
}
