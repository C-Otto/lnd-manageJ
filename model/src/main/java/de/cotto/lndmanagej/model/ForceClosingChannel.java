package de.cotto.lndmanagej.model;

public final class ForceClosingChannel extends LocalChannel {
    public ForceClosingChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey);
    }
}
