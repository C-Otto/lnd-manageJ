package de.cotto.lndmanagej.model;

import javax.annotation.Nullable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record Node(String alias, int lastUpdate, String pubkey) implements Comparable<Node> {

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return alias;
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

        private int lastUpdate;

        @Nullable
        private String pubkey;

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder withLastUpdate(int lastUpdate) {
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
