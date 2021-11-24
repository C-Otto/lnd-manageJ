package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.OpenCloseStatus.WAITING_CLOSE;

public class WaitingCloseChannel extends LocalChannel {
    public WaitingCloseChannel(
            ChannelId channelId,
            ChannelPoint channelPoint,
            Coins capacity,
            Pubkey ownPubkey,
            Pubkey remotePubkey,
            OpenInitiator openInitiator
    ) {
        super(channelId, channelPoint, capacity, ownPubkey, remotePubkey, openInitiator, false);
    }

    @Override
    public ChannelStatus getStatus() {
        return new ChannelStatus(isPrivateChannel(), false, false, WAITING_CLOSE);
    }
}
