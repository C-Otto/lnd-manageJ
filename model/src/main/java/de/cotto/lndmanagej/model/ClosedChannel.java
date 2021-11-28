package de.cotto.lndmanagej.model;

import java.util.Objects;

import static de.cotto.lndmanagej.model.OpenCloseStatus.CLOSED;

public abstract class ClosedChannel extends ClosedOrClosingChannel {
    private final CloseInitiator closeInitiator;
    private final int closeHeight;

    public ClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator,
            int closeHeight
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator
        );
        this.closeInitiator = closeInitiator;
        if (closeHeight == 0) {
            throw new IllegalArgumentException("Close height must be set");
        }
        this.closeHeight = closeHeight;
    }

    public CloseInitiator getCloseInitiator() {
        return closeInitiator;
    }

    public int getCloseHeight() {
        return closeHeight;
    }

    @Override
    public ClosedChannel getAsClosedChannel() {
        return this;
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(isPrivateChannel(), false, true, CLOSED);
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
        return closeHeight == that.closeHeight && closeInitiator == that.closeInitiator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), closeInitiator, closeHeight);
    }
}
