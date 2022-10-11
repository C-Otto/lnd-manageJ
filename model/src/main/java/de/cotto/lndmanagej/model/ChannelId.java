package de.cotto.lndmanagej.model;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Objects;

public final class ChannelId implements Comparable<ChannelId> {
    private static final int EXPECTED_NUMBER_OF_SEGMENTS = 3;

    private final BigInteger shortChannelId;

    private ChannelId(BigInteger shortChannelId) {
        this.shortChannelId = shortChannelId;
    }

    public static ChannelId fromShortChannelId(long shortChannelId) {
        return fromShortChannelId(BigInteger.valueOf(shortChannelId));
    }

    public static ChannelId fromShortChannelId(BigInteger shortChannelId) {
        if (shortChannelId.signum() <= 0 || shortChannelId.bitLength() > 64) {
            throw new IllegalArgumentException("Illegal channel ID " + shortChannelId);
        }
        return new ChannelId(shortChannelId);
    }

    @SuppressWarnings("StringSplitter")
    public static ChannelId fromCompactForm(String compactForm) {
        String[] split = compactForm.split("[x:]");
        if (split.length != EXPECTED_NUMBER_OF_SEGMENTS) {
            throw new IllegalArgumentException("Unexpected format for compact channel ID");
        }
        BigInteger block = new BigInteger(split[0]);
        BigInteger transaction = new BigInteger(split[1]);
        BigInteger output = new BigInteger(split[2]);
        BigInteger shortChannelId = block.shiftLeft(40).or(transaction.shiftLeft(16)).or(output);
        return fromShortChannelId(shortChannelId);
    }

    public String getCompactForm() {
        return getCompactFormWithDelimiter("x");
    }

    public String getCompactFormLnd() {
        return getCompactFormWithDelimiter(":");
    }

    public long getShortChannelId() {
        return shortChannelId.longValue();
    }

    public int getBlockHeight() {
        return (int) shortChannelId.shiftRight(40).longValue();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ChannelId channelId = (ChannelId) other;
        return Objects.equals(shortChannelId, channelId.shortChannelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortChannelId);
    }

    @Override
    public String toString() {
        return getCompactForm();
    }

    @Override
    public int compareTo(@Nonnull ChannelId other) {
        return shortChannelId.compareTo(other.shortChannelId);
    }

    private String getCompactFormWithDelimiter(String delimiter) {
        long block = getBlockHeight();
        BigInteger transaction = shortChannelId.shiftRight(16).and(BigInteger.valueOf(16_777_215));
        BigInteger output = shortChannelId.and(BigInteger.valueOf(65_535));
        return block + delimiter + transaction + delimiter + output;
    }
}
