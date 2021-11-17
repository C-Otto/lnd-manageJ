package de.cotto.lndmanagej.model;

import javax.annotation.Nullable;

public abstract class ClosedChannelBuilder<T extends ClosedChannel> {
    @Nullable
    ChannelId channelId;

    @Nullable
    ChannelPoint channelPoint;

    @Nullable
    Coins capacity;

    @Nullable
    Pubkey ownPubkey;

    @Nullable
    Pubkey remotePubkey;

    @Nullable
    String closeTransactionHash;

    @Nullable
    OpenInitiator openInitiator;

    @Nullable
    CloseInitiator closeInitiator;

    public ClosedChannelBuilder() {
        // default constructor
    }

    public ClosedChannelBuilder<T> withChannelId(ChannelId channelId) {
        this.channelId = channelId;
        return this;
    }

    public ClosedChannelBuilder<T> withChannelPoint(ChannelPoint channelPoint) {
        this.channelPoint = channelPoint;
        return this;
    }

    public ClosedChannelBuilder<T> withCapacity(Coins capacity) {
        this.capacity = capacity;
        return this;
    }

    public ClosedChannelBuilder<T> withOwnPubkey(Pubkey ownPubkey) {
        this.ownPubkey = ownPubkey;
        return this;
    }

    public ClosedChannelBuilder<T> withRemotePubkey(Pubkey remotePubkey) {
        this.remotePubkey = remotePubkey;
        return this;
    }

    public ClosedChannelBuilder<T> withCloseTransactionHash(String closeTransactionHash) {
        this.closeTransactionHash = closeTransactionHash;
        return this;
    }

    public ClosedChannelBuilder<T> withOpenInitiator(OpenInitiator openInitiator) {
        this.openInitiator = openInitiator;
        return this;
    }

    public ClosedChannelBuilder<T> withCloseInitiator(CloseInitiator closeInitiator) {
        this.closeInitiator = closeInitiator;
        return this;
    }

    public abstract T build();
}
