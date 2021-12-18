package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

import static de.cotto.lndmanagej.model.OpenCloseStatus.FORCE_CLOSING;

public final class ForceClosingChannel extends ClosedOrClosingChannel {
    private final Set<ChannelPoint> htlcOutpoints;

    public ForceClosingChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            TransactionHash closeTransactionHash,
            Set<ChannelPoint> htlcOutpoints,
            OpenInitiator openInitiator,
            boolean isPrivate
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, closeTransactionHash, openInitiator, isPrivate);
        this.htlcOutpoints = htlcOutpoints;
    }

    public Set<ChannelPoint> getHtlcOutpoints() {
        return htlcOutpoints;
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(false, false, false, FORCE_CLOSING);
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
