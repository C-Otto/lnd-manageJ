package de.cotto.lndmanagej.model;

import java.util.Set;

public class BreachForceClosedChannel extends ForceClosedChannel {
    public BreachForceClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            TransactionHash closeTransactionHash,
            OpenInitiator openInitiator,
            int closeHeight,
            Set<Resolution> resolutions
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                CloseInitiator.REMOTE,
                closeHeight,
                resolutions
        );
    }

    @Override
    public boolean isBreach() {
        return true;
    }
}
