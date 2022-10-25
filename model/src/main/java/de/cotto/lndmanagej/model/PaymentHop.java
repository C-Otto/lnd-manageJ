package de.cotto.lndmanagej.model;

public record PaymentHop(ChannelId channelId, Coins amount, boolean first) {
    public boolean last() {
        return !first;
    }
}
