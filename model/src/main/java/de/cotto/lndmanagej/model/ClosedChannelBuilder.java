package de.cotto.lndmanagej.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nullable;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.TooManyMethods")
public abstract class ClosedChannelBuilder<T extends ClosedChannel> {
    @Nullable
    private ChannelId channelId;

    @Nullable
    private ChannelPoint channelPoint;

    @Nullable
    private Coins capacity;

    @Nullable
    Pubkey ownPubkey;

    @Nullable
    Pubkey remotePubkey;

    @Nullable
    TransactionHash closeTransactionHash;

    @Nullable
    OpenInitiator openInitiator;

    @Nullable
    CloseInitiator closeInitiator;

    Set<Resolution> resolutions = Set.of();

    int closeHeight;

    boolean isPrivate;

    public ClosedChannelBuilder() {
        // default constructor
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withChannelId(ChannelId channelId) {
        this.channelId = channelId;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withChannelPoint(ChannelPoint channelPoint) {
        this.channelPoint = channelPoint;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withCapacity(Coins capacity) {
        this.capacity = capacity;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withOwnPubkey(Pubkey ownPubkey) {
        this.ownPubkey = ownPubkey;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withRemotePubkey(Pubkey remotePubkey) {
        this.remotePubkey = remotePubkey;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withCloseTransactionHash(TransactionHash closeTransactionHash) {
        this.closeTransactionHash = closeTransactionHash;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withOpenInitiator(OpenInitiator openInitiator) {
        this.openInitiator = openInitiator;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withCloseInitiator(CloseInitiator closeInitiator) {
        this.closeInitiator = closeInitiator;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withCloseHeight(int closeHeight) {
        this.closeHeight = closeHeight;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withResolutions(Set<Resolution> resolutions) {
        this.resolutions = resolutions;
        return this;
    }

    @CanIgnoreReturnValue
    public ClosedChannelBuilder<T> withIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        return this;
    }

    protected ChannelCoreInformation getChannelCoreInformation() {
        return new ChannelCoreInformation(
                requireNonNull(channelId),
                requireNonNull(channelPoint),
                requireNonNull(capacity)
        );
    }

    public abstract T build();
}
