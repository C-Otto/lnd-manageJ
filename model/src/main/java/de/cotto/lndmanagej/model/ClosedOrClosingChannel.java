package de.cotto.lndmanagej.model;

import java.util.Objects;

public abstract class ClosedOrClosingChannel extends LocalChannel {
    private final TransactionHash closeTransactionHash;

    protected ClosedOrClosingChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            TransactionHash closeTransactionHash,
            OpenInitiator openInitiator,
            boolean isPrivate
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, openInitiator, isPrivate);
        this.closeTransactionHash = closeTransactionHash;
    }

    public TransactionHash getCloseTransactionHash() {
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
