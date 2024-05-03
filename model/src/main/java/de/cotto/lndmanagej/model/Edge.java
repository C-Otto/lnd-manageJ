package de.cotto.lndmanagej.model;

public record Edge(
        ChannelId channelId,
        Pubkey startNode,
        Pubkey endNode,
        Coins capacity,
        Policy policy,
        Policy reversePolicy
) {
    public Edge withCapacity(Coins capacity) {
        return new Edge(channelId(), startNode(), endNode(), capacity, policy(), reversePolicy());
    }
}
