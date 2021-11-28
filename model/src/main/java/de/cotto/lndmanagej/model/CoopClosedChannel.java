package de.cotto.lndmanagej.model;

public class CoopClosedChannel extends ClosedChannel {
    public CoopClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            String closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator,
            int closeHeight
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                closeInitiator,
                closeHeight
        );
    }
}
