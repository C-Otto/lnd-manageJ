package de.cotto.lndmanagej.model;

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Channel {
    private final ChannelId channelId;
    private final Coins capacity;
    private final ChannelPoint channelPoint;
    private final Set<Pubkey> pubkeys;

    private Channel(ChannelId channelId, Coins capacity, ChannelPoint channelPoint, Pubkey pubkey1, Pubkey pubkey2) {
        this(channelId, capacity, channelPoint, List.of(pubkey1, pubkey2));
    }

    protected Channel(ChannelId channelId, Coins capacity, ChannelPoint channelPoint, Collection<Pubkey> pubkeys) {
        this.channelId = channelId;
        this.capacity = Coins.ofMilliSatoshis(capacity.milliSatoshis());
        this.channelPoint = channelPoint;
        this.pubkeys = new LinkedHashSet<>(pubkeys);
    }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        @Nullable
        private ChannelId channelId;

        @Nullable
        private Coins capacity;

        @Nullable
        private ChannelPoint channelPoint;

        @Nullable
        private Pubkey pubkey1;

        @Nullable
        private Pubkey pubkey2;

        public Builder withChannelId(ChannelId channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withCapacity(Coins capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder withChannelPoint(ChannelPoint channelPoint) {
            this.channelPoint = channelPoint;
            return this;
        }

        public Builder withNode1(Pubkey pubkey) {
            this.pubkey1 = pubkey;
            return this;
        }

        public Builder withNode2(Pubkey pubkey) {
            pubkey2 = pubkey;
            return this;
        }

        public Channel build() {
            if (requireNonNull(pubkey1).equals(requireNonNull(pubkey2))) {
                throw new IllegalArgumentException("Pubkeys must not be the same");
            }
            return new Channel(
                    requireNonNull(channelId),
                    requireNonNull(capacity),
                    requireNonNull(channelPoint),
                    requireNonNull(pubkey1),
                    requireNonNull(pubkey2)
            );
        }
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

    @Override
    public String toString() {
        return "Channel[" +
                "channelId=" + channelId +
                ", capacity=" + capacity +
                ", channelPoint=" + channelPoint +
                ", pubkeys=" + pubkeys +
                ']';
    }
}
