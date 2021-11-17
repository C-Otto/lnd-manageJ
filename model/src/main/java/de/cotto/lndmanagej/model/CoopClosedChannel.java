package de.cotto.lndmanagej.model;

public class CoopClosedChannel extends ClosedChannel {
    public CoopClosedChannel(
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
