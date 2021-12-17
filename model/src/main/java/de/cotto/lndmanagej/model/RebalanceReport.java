package de.cotto.lndmanagej.model;

public record RebalanceReport(
        Coins sourceCost,
        Coins sourceAmount,
        Coins targetCost,
        Coins targetAmount
) {
    public static final RebalanceReport EMPTY = new RebalanceReport(Coins.NONE, Coins.NONE, Coins.NONE, Coins.NONE);
}
