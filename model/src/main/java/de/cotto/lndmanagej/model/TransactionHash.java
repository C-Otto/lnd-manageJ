package de.cotto.lndmanagej.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class TransactionHash {
    private static final Pattern PATTERN = Pattern.compile("[0-9a-fA-F]{64}");
    private final String hash;

    private TransactionHash(String hash) {
        this.hash = hash;
    }

    public static TransactionHash create(String string) {
        if (!PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException("Transaction hash must have 64 hex characters");
        }
        return new TransactionHash(string.toLowerCase(Locale.US));
    }

    public String getHash() {
        return hash;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TransactionHash that = (TransactionHash) other;
        return Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    @Override
    public String toString() {
        return hash;
    }

}
