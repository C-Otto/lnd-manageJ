package de.cotto.lndmanagej.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record Node(
        @Nonnull Pubkey pubkey,
        @Nonnull String alias,
        int lastUpdate,
        boolean online
) implements Comparable<Node> {

    public Node(Pubkey pubkey, String alias, int lastUpdate, boolean online) {
        this.online = online;
        this.alias = alias;
        this.lastUpdate = lastUpdate;
        this.pubkey = pubkey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Node forPubkey(Pubkey pubkey) {
        return Node.builder().withPubkey(pubkey).build();
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
        private Pubkey pubkey;

        private boolean online;

        @CanIgnoreReturnValue
        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withLastUpdate(int lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withPubkey(Pubkey pubkey) {
            this.pubkey = pubkey;
            if (alias == null) {
                alias = pubkey.toString();
            }
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withOnlineStatus(boolean online) {
            this.online = online;
            return this;
        }

        public Node build() {
            return new Node(requireNonNull(pubkey), requireNonNull(alias), lastUpdate, online);
        }
    }
}
