package de.cotto.lndmanagej.graph.model;

import org.springframework.lang.Nullable;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Channel {
    private final ChannelId channelId;
    private final Coins capacity;
    private final Set<Node> nodes = new LinkedHashSet<>();

    private Channel(ChannelId channelId, Coins capacity, Node node1, Node node2) {
        this.channelId = channelId;
        this.capacity = Coins.ofMilliSatoshis(capacity.getMilliSatoshis());
        nodes.add(node1);
        nodes.add(node2);
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

    public Set<Node> getNodes() {
        return nodes;
    }

    public static class Builder {
        @Nullable
        private ChannelId channelId;

        @Nullable
        private Coins capacity;

        @Nullable
        private Node node1;

        @Nullable
        private Node node2;

        public Builder withChannelId(ChannelId channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder withCapacity(Coins capacity) {
            this.capacity = capacity;
            return this;
        }

        public Builder withNode1(Node node) {
            node1 = node;
            return this;
        }

        public Builder withNode2(Node node) {
            node2 = node;
            return this;
        }

        public Channel build() {
            return new Channel(
                    requireNonNull(channelId),
                    requireNonNull(capacity),
                    requireNonNull(node1),
                    requireNonNull(node2)
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
                && Objects.equals(nodes, channel.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, capacity, nodes);
    }
}
