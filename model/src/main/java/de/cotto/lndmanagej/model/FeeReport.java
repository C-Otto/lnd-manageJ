package de.cotto.lndmanagej.model;

public record FeeReport(Coins earned, Coins sourced) {
    public static final FeeReport EMPTY = new FeeReport(Coins.NONE, Coins.NONE);
}
