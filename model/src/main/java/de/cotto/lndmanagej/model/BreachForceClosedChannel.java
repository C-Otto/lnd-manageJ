package de.cotto.lndmanagej.model;

public class BreachForceClosedChannel extends ForceClosedChannel {
    public BreachForceClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                CloseInitiator.REMOTE
        );
    }
}
