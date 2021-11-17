package de.cotto.lndmanagej.model;

import java.util.Objects;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;
    private final OpenInitiator openInitiator;

    protected LocalChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            OpenInitiator openInitiator
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey);
        this.remotePubkey = remotePubkey;
        this.openInitiator = openInitiator;
    }

    public Pubkey getRemotePubkey() {
        return remotePubkey;
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
        LocalChannel that = (LocalChannel) other;
        return Objects.equals(remotePubkey, that.remotePubkey) && openInitiator == that.openInitiator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), remotePubkey, openInitiator);
    }

    public OpenInitiator getOpenInitiator() {
        return openInitiator;
    }
}
