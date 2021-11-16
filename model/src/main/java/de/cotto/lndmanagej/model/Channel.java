package de.cotto.lndmanagej.model;

import java.util.Objects;
import java.util.Set;

public class Channel {
    private final ChannelId channelId;
    private final Coins capacity;
    private final ChannelPoint channelPoint;
    private final Set<Pubkey> pubkeys;

    protected Channel(ChannelId channelId, ChannelPoint channelPoint, Coins capacity, Pubkey pubkey1, Pubkey pubkey2) {
        if (pubkey1.equals(pubkey2)) {
            throw new IllegalArgumentException("Pubkeys must not be the same");
        }
        this.channelId = channelId;
        this.capacity = capacity;
        this.channelPoint = channelPoint;
        this.pubkeys = Set.of(pubkey1, pubkey2);
    }

    public Coins getCapacity() {
        return capacity;
    }

    public ChannelId getId() {
        return channelId;
    }

    public Set<Pubkey> getPubkeys() {
        return pubkeys;
    }

    public ChannelPoint getChannelPoint() {
        return channelPoint;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Channel channel = (Channel) other;
        return Objects.equals(channelId, channel.channelId)
                && Objects.equals(capacity, channel.capacity)
                && Objects.equals(channelPoint, channel.channelPoint)
                && Objects.equals(pubkeys, channel.pubkeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, capacity, channelPoint, pubkeys);
    }
}
