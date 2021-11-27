package de.cotto.lndmanagej.model;

import java.util.Objects;

public abstract class ClosedOrClosingChannel extends LocalChannel {
    private final String closeTransactionHash;

    protected ClosedOrClosingChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, openInitiator, false);
        this.closeTransactionHash = closeTransactionHash;
    }

    public String getCloseTransactionHash() {
        return closeTransactionHash;
    }

    @Override
    public Coins getTotalReceived() {
        return Coins.NONE;
    }

    @Override
    public Coins getTotalSent() {
        return Coins.NONE;
    }

    @Override
    @SuppressWarnings("CPD-START")
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
        ClosedOrClosingChannel that = (ClosedOrClosingChannel) other;
        return Objects.equals(closeTransactionHash, that.closeTransactionHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), closeTransactionHash);
    }
}
