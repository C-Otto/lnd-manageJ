package de.cotto.lndmanagej.model;

public class CoopClosedChannel extends ClosedChannel {
    public CoopClosedChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            TransactionHash closeTransactionHash,
            OpenInitiator openInitiator,
            CloseInitiator closeInitiator,
            int closeHeight,
            boolean isPrivate
    ) {
        super(
                channelCoreInformation,
                ownPubkey,
                remotePubkey,
                closeTransactionHash,
                openInitiator,
                closeInitiator,
                closeHeight,
                isPrivate
        );
    }
}
