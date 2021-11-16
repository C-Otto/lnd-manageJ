package de.cotto.lndmanagej.model;

public final class ForceClosingChannel extends ClosedOrClosingChannel {
    public ForceClosingChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey, closeTransactionHash);
    }
}
