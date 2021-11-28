package de.cotto.lndmanagej.model;

public class BreachForceClosedChannel extends ForceClosedChannel {
    public BreachForceClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator,
            int closeHeight
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                CloseInitiator.REMOTE,
                closeHeight
        );
    }
}
