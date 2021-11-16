package de.cotto.lndmanagej.model;

public final class ClosedChannel extends LocalChannel {
    public ClosedChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey);
    }
}
