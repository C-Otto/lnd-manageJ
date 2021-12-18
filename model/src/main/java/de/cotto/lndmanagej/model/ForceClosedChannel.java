package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

public class ForceClosedChannel extends ClosedChannel {
    private final Set<Resolution> resolutions;

    public ForceClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            TransactionHash closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator,
            int closeHeight,
            Set<Resolution> resolutions,
            boolean isPrivate
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                closeInitiator,
                closeHeight,
                isPrivate
        );
        this.resolutions = resolutions;
    }

    public Set<Resolution> getResolutions() {
        return resolutions;
    }

    @Override
    public boolean isForceClosed() {
        return true;
    }

    @Override
    public ForceClosedChannel getAsForceClosedChannel() {
        return this;
    }

    public boolean isBreach() {
        return false;
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
        ForceClosedChannel that = (ForceClosedChannel) other;
        return Objects.equals(resolutions, that.resolutions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), resolutions);
    }
}
