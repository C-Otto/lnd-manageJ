package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.OpenCloseStatus.WAITING_CLOSE;

public class WaitingCloseChannel extends LocalChannel {
    public WaitingCloseChannel(
            ChannelCoreInformation channelCoreInformation,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            OpenInitiator openInitiator,
            boolean isPrivate
    ) {
        super(channelCoreInformation, ownPubkey, remotePubkey, openInitiator, isPrivate);
    }

    @Override
    public Coins getTotalSent() {
        return Coins.NONE;
    }

    @Override
    public Coins getTotalReceived() {
        return Coins.NONE;
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(isPrivateChannel(), false, false, WAITING_CLOSE);
    }
}
