package de.cotto.lndmanagej.model;

public final class ClosedChannel extends LocalChannel {
    private ClosedChannel(LocalChannel localChannel) {
        super(localChannel, localChannel.getRemotePubkey());
    }

    public static ClosedChannel create(UnresolvedClosedChannel unresolvedClosedChannel) {
        return create(unresolvedClosedChannel, unresolvedClosedChannel.getId());
    }

    public static ClosedChannel create(UnresolvedClosedChannel unresolvedClosedChannel, ChannelId channelId) {
        if (channelId.isUnresolved()) {
            throw new IllegalArgumentException("Channel ID must be resolved");
        }
        return new ClosedChannel(unresolvedClosedChannel.getWithId(channelId));
    }
}
