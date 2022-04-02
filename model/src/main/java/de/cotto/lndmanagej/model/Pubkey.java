package de.cotto.lndmanagej.model;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;

public final class Pubkey implements Comparable<Pubkey> {
    private static final int EXPECTED_NUMBER_OF_BYTES = 33;

    private final String string;
    private final byte[] byteArray;
    private final int hash;

    private Pubkey(String string) {
        this.string = string;
        byteArray = HexFormat.of().parseHex(string);
        hash = Arrays.hashCode(byteArray);
        if (byteArray.length != EXPECTED_NUMBER_OF_BYTES) {
            throw new IllegalArgumentException();
        }
    }

    public static Pubkey create(String string) {
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
        return Arrays.equals(byteArray, pubkey.byteArray);
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
