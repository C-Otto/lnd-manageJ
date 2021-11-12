package de.cotto.lndmanagej.model;

import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Channel {
    private final ChannelId channelId;
    private final Coins capacity;
    private final Set<Pubkey> pubkeys = new LinkedHashSet<>();

    private Channel(ChannelId channelId, Coins capacity, Pubkey pubkey1, Pubkey pubkey2) {
        this.channelId = channelId;
        this.capacity = Coins.ofMilliSatoshis(capacity.milliSatoshis());
        pubkeys.add(pubkey1);
        pubkeys.add(pubkey2);
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

    public static class Builder {
        @Nullable
        private ChannelId channelId;

        @Nullable
        private Coins capacity;

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

        public Builder withNode1(Pubkey pubkey) {
            this.pubkey1 = pubkey;
            return this;
        }

        public Builder withNode2(Pubkey pubkey) {
            pubkey2 = pubkey;
            return this;
        }

        public Channel build() {
            return new Channel(
                    requireNonNull(channelId),
                    requireNonNull(capacity),
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
                && Objects.equals(pubkeys, channel.pubkeys);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, capacity, pubkeys);
    }

    @Override
    public String toString() {
        return "Channel[" +
                "channelId=" + channelId +
                ", capacity=" + capacity +
                ", pubkeys=" + pubkeys +
                ']';
    }
}
