package de.cotto.lndmanagej.model;

public record DirectedChannelEdge(ChannelId channelId, Coins capacity, Pubkey source, Pubkey target, Policy policy) {
}
