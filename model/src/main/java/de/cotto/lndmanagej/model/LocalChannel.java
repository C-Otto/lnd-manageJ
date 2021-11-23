package de.cotto.lndmanagej.model;

import java.util.Objects;

public class LocalChannel extends Channel {
    private final Pubkey remotePubkey;
    private final OpenInitiator openInitiator;
    private final boolean privateChannel;

    protected LocalChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            OpenInitiator openInitiator,
            boolean privateChannel
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey);
        this.remotePubkey = remotePubkey;
        this.openInitiator = openInitiator;
        this.privateChannel = privateChannel;
    }

    public Pubkey getRemotePubkey() {
        return remotePubkey;
    }

    public OpenInitiator getOpenInitiator() {
        return openInitiator;
    }

    public boolean isPrivateChannel() {
        return privateChannel;
    }

    public boolean isActive() {
        return false;
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
        return privateChannel == that.privateChannel
                && Objects.equals(remotePubkey, that.remotePubkey)
                && openInitiator == that.openInitiator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), remotePubkey, openInitiator, privateChannel);
    }
}
