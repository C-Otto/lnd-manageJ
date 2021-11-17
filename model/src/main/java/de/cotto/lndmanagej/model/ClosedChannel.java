package de.cotto.lndmanagej.model;

import java.util.Objects;

public abstract class ClosedChannel extends ClosedOrClosingChannel {
    private final CloseInitiator closeInitiator;

    public ClosedChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator
    ) {
        super(
                channelId,
                channelPoint,
                capacity,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator
        );
        this.closeInitiator = closeInitiator;
    }

    public CloseInitiator getCloseInitiator() {
        return closeInitiator;
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
        ClosedChannel that = (ClosedChannel) other;
        return closeInitiator == that.closeInitiator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), closeInitiator);
    }
}
