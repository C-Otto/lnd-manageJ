package de.cotto.lndmanagej.model;

public record OnChainCosts(Coins open, Coins close) {
    public static final OnChainCosts NONE = new OnChainCosts(Coins.NONE, Coins.NONE);

    public OnChainCosts add(OnChainCosts other) {
        return new OnChainCosts(open.add(other.open), close.add(other.close));
    }
}
