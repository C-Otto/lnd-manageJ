package de.cotto.lndmanagej.model;

import java.util.Objects;

public class LocalOpenChannel extends LocalChannel {
    private final BalanceInformation balanceInformation;

    public LocalOpenChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            BalanceInformation balanceInformation
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey);
        this.balanceInformation = balanceInformation;
    }

    public BalanceInformation getBalanceInformation() {
        return balanceInformation;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        if (!super.equals(other)) {
            return false;
        }
        LocalOpenChannel that = (LocalOpenChannel) other;
        return Objects.equals(balanceInformation, that.balanceInformation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balanceInformation);
    }
}
