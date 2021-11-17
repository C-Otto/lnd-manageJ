package de.cotto.lndmanagej.model;

import java.util.Objects;

public class ForceClosedChannel extends ClosedChannel {
    private final CloseType closeType;

    public ForceClosedChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            CloseType closeType
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey, closeTransactionHash);
        this.closeType = closeType;
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
        ForceClosedChannel that = (ForceClosedChannel) other;
        return closeType == that.closeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), closeType);
    }
}
