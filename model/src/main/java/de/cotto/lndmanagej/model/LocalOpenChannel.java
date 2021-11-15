package de.cotto.lndmanagej.model;

public class LocalOpenChannel extends LocalChannel {
    private final BalanceInformation balanceInformation;

    public LocalOpenChannel(Channel channel, Pubkey ownPubkey, BalanceInformation balanceInformation) {
        super(channel, ownPubkey);
        this.balanceInformation = balanceInformation;
    }

    public BalanceInformation getBalanceInformation() {
        return balanceInformation;
    }
}
