package de.cotto.lndmanagej.model;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ChannelId implements Comparable<ChannelId> {
    private static final int EXPECTED_NUMBER_OF_SEGMENTS = 3;
    private static final long NOT_BEFORE = 430_103_660_018_532_352L; // January 1st 2016

    private final long shortChannelId;

    private ChannelId(long shortChannelId) {
        this.shortChannelId = shortChannelId;
    }

    public static ChannelId fromShortChannelId(long shortChannelId) {
        if (shortChannelId < NOT_BEFORE) {
            throw new IllegalArgumentException("Illegal channel ID");
        }
        return new ChannelId(shortChannelId);
    }

    @SuppressWarnings("StringSplitter")
    public static ChannelId fromCompactForm(String compactForm) {
        String[] split = compactForm.split("[x:]");
        if (split.length != EXPECTED_NUMBER_OF_SEGMENTS) {
            throw new IllegalArgumentException("Unexpected format for compact channel ID");
        }
        long block = Long.parseLong(split[0]);
        long transaction = Long.parseLong(split[1]);
        long output = Long.parseLong(split[2]);
        long shortChannelId = (block << 40) | (transaction << 16) | output;
        return fromShortChannelId(shortChannelId);
    }

    public String getCompactForm() {
        return getCompactFormWithDelimiter("x");
    }

    public String getCompactFormLnd() {
        return getCompactFormWithDelimiter(":");
    }

    public long getShortChannelId() {
        return shortChannelId;
    }

    public int getBlockHeight() {
        return (int) (shortChannelId >> 40);
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
        return shortChannelId == channelId.shortChannelId;
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
        return Long.compare(shortChannelId, other.shortChannelId);
    }

    private String getCompactFormWithDelimiter(String delimiter) {
        long block = getBlockHeight();
        long transaction = shortChannelId >> 16 & 0xFFFFFF;
        long output = shortChannelId & 0xFFFF;
        return block + delimiter + transaction + delimiter + output;
    }
}
