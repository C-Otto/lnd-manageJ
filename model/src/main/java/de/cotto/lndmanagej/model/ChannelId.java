package de.cotto.lndmanagej.model;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ChannelId implements Comparable<ChannelId> {
    public static final ChannelId UNRESOLVED = new ChannelId(-1);
    private final long shortChannelId;
    private static final int EXPECTED_NUMBER_OF_SEGMENTS = 3;
    private static final long NOT_BEFORE = 430_103_660_018_532_352L; // January 1st 2016

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
        if (isUnresolved()) {
            throw new IllegalStateException("Channel ID must be resolved");
        }
        long block = shortChannelId >> 40;
        long transaction = shortChannelId >> 16 & 0xFFFFFF;
        long output = shortChannelId & 0xFFFF;
        return block + ":" + transaction + ":" + output;
    }

    public long getShortChannelId() {
        if (isUnresolved()) {
            throw new IllegalStateException("Channel ID must be resolved");
        }
        return shortChannelId;
    }

    public boolean isUnresolved() {
        return UNRESOLVED.equals(this);
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
        if (isUnresolved()) {
            return "UNRESOLVED_CHANNEL_ID";
        }
        return String.valueOf(shortChannelId);
    }

    @Override
    public int compareTo(@Nonnull ChannelId other) {
        if ((isUnresolved() || other.isUnresolved()) && !this.equals(other)) {
            throw new IllegalStateException("Cannot compare with unresolved channel ID");
        }
        return Long.compare(shortChannelId, other.shortChannelId);
    }
}
