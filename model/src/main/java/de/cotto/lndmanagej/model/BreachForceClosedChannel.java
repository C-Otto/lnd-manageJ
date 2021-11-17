package de.cotto.lndmanagej.model;

public class BreachForceClosedChannel extends ForceClosedChannel {
    public BreachForceClosedChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator
    ) {
        super(
                channelId,
                channelPoint,
                capacity,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                CloseInitiator.REMOTE
        );
    }
}
