package de.cotto.lndmanagej.model;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

public class HexString {
    public static final HexString EMPTY = new HexString("");

    private final byte[] byteArray;
    private final String string;

    public HexString(byte... bytes) {
        byteArray = Arrays.copyOf(bytes, bytes.length);
        string = HexFormat.of().formatHex(byteArray);
    }

    public HexString(String string) {
        this(stringToByteArray(string));
    }

    public HexString(List<Byte> byteList) {
        this(listToByteArray(byteList));
    }

    public int getNumberOfBytes() {
        return byteArray.length;
    }

    public byte[] getByteArray() {
        return Arrays.copyOf(byteArray, byteArray.length);
    }

    public int getStringLength() {
        return string.length();
    }

    private static byte[] stringToByteArray(String string) {
        return HexFormat.of().parseHex(string);
    }

    private static byte[] listToByteArray(List<Byte> byteList) {
        byte[] byteArray = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++) {
            byteArray[i] = byteList.get(i);
        }
        return byteArray;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        HexString hexString = (HexString) other;
        return Arrays.equals(byteArray, hexString.byteArray) && Objects.equals(string, hexString.string);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(string);
        result = 31 * result + Arrays.hashCode(byteArray);
        return result;
    }

    @Override
    public String toString() {
        return string;
    }
}
