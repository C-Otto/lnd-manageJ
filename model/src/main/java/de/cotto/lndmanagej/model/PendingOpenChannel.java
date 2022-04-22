package de.cotto.lndmanagej.model;

public record PendingOpenChannel(
        ChannelPoint channelPoint,
        Coins capacity,
        Pubkey ownPubkey,
        Pubkey remotePubkey,
        OpenInitiator openInitiator,
        boolean isPrivate
) {
}