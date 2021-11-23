package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

public final class ForceClosingChannel extends ClosedOrClosingChannel {
    private final Set<ChannelPoint> htlcOutpoints;

    public ForceClosingChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            Set<ChannelPoint> htlcOutpoints,
            OpenInitiator openInitiator
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey, closeTransactionHash, openInitiator);
        this.htlcOutpoints = htlcOutpoints;
    }

    public Set<ChannelPoint> getHtlcOutpoints() {
        return htlcOutpoints;
    }

    @Override
    public boolean isClosed() {
        return true;
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
        ForceClosingChannel that = (ForceClosingChannel) other;
        return Objects.equals(htlcOutpoints, that.htlcOutpoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), htlcOutpoints);
    }
}
