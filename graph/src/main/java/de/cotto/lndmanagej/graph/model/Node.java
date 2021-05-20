package de.cotto.lndmanagej.graph.model;

import javax.annotation.Nullable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class Node implements Comparable<Node> {
    private final String alias;
    private final long lastUpdate;
    private final String pubkey;

    private Node(String alias, long lastUpdate, String pubkey) {
        this.alias = alias;
        this.lastUpdate = lastUpdate;
        this.pubkey = pubkey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return alias;
    }

    public String getPubkey() {
        return pubkey;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Node node = (Node) other;
        return Objects.equals(pubkey, node.pubkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pubkey);
    }

    @Override
    public int compareTo(Node other) {
        return pubkey.compareTo(other.pubkey);
    }

    public static class Builder {
        @Nullable
        private String alias;

        private long lastUpdate;

        @Nullable
        private String pubkey;

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder withLastUpdate(long lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Builder withPubkey(String pubkey) {
            this.pubkey = pubkey;
            if (alias == null) {
                alias = pubkey;
            }
            return this;
        }

        public Node build() {
            return new Node(requireNonNull(alias), lastUpdate, requireNonNull(pubkey));
        }
    }
}
