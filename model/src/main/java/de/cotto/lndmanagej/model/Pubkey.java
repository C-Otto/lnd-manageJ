package de.cotto.lndmanagej.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Pubkey implements Comparable<Pubkey> {
    private static final Pattern PATTERN = Pattern.compile("[0-9a-fA-F]{66}");
    private final String string;

    private Pubkey(String string) {
        this.string = string;
    }

    public static Pubkey create(String string) {
        if (!PATTERN.matcher(string).matches()) {
            throw new IllegalArgumentException("Pubkey must have 66 hex characters");
        }
        return new Pubkey(string.toLowerCase(Locale.US));
    }

    @Override
    public int compareTo(Pubkey other) {
        return string.compareTo(other.string);
    }

    @Override
    public String toString() {
        return string;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Pubkey pubkey = (Pubkey) other;
        return Objects.equals(string, pubkey.string);
    }

    @Override
    public int hashCode() {
        return Objects.hash(string);
    }
}
