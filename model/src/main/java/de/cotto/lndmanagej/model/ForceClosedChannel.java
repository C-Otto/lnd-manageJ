package de.cotto.lndmanagej.model;

public class ForceClosedChannel extends ClosedChannel {
    public ForceClosedChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator
    ) {
        super(
                channelId,
                channelPoint,
                capacity,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                closeInitiator
        );
    }
}
