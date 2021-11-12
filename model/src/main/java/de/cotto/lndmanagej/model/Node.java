package de.cotto.lndmanagej.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public record Node(@Nonnull String alias, int lastUpdate, @Nonnull Pubkey pubkey) implements Comparable<Node> {
    private static final Map<Pubkey, String> HARDCODED_ALIASES = Map.of(
            Pubkey.create("02f72978d40efeffca537139ad6ac9f09970c000a2dbc0d7aa55a71327c4577a80"), "Chivo IBEX_a0",
            Pubkey.create("037cc5f9f1da20ac0d60e83989729a204a33cc2d8e80438969fadf35c1c5f1233b"), "BlueWallet"
    );

    public Node(String alias, int lastUpdate, Pubkey pubkey) {
        this.alias = HARDCODED_ALIASES.getOrDefault(pubkey, alias);
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

        public Builder withAlias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder withLastUpdate(int lastUpdate) {
            this.lastUpdate = lastUpdate;
            return this;
        }

        public Builder withPubkey(Pubkey pubkey) {
            this.pubkey = pubkey;
            if (alias == null) {
                alias = pubkey.toString();
            }
            return this;
        }

        public Node build() {
            return new Node(requireNonNull(alias), lastUpdate, requireNonNull(pubkey));
        }

    }
}
