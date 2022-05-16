package de.cotto.lndmanagej.model;

public record RouteHint(
        Pubkey sourceNode,
        Pubkey endNode,
        ChannelId channelId,
        Coins baseFee,
        long feeRate,
        int cltvExpiryDelta
) {
}
