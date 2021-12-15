package de.cotto.lndmanagej.model;

public record OffChainCosts(
        Coins rebalanceSource,
        Coins rebalanceTarget
) {
    public static final OffChainCosts NONE = new OffChainCosts(Coins.NONE, Coins.NONE);

    public OffChainCosts add(OffChainCosts other) {
        return new OffChainCosts(
                rebalanceSource.add(other.rebalanceSource),
                rebalanceTarget.add(other.rebalanceTarget)
        );
    }
}
