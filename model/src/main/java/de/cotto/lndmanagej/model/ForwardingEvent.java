package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;

public record ForwardingEvent(
        int index,
        Coins amountIn,
        Coins amountOut,
        ChannelId channelIn,
        ChannelId channelOut,
        LocalDateTime timestamp
) {
    public Coins fees() {
        return amountIn.subtract(amountOut);
    }
}
