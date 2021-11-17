package de.cotto.lndmanagej.model;

public class WaitingCloseChannel extends LocalChannel {
    public WaitingCloseChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            OpenInitiator openInitiator
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey, openInitiator);
    }
}
