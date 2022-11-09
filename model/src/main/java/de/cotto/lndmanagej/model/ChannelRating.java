package de.cotto.lndmanagej.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ChannelRating implements Rating {
    private final ChannelId channelId;
    private final long value;
    private final Map<String, Number> descriptions;

    public ChannelRating(ChannelId channelId) {
        this(channelId, 0, Map.of());
    }

    private ChannelRating(ChannelId channelId, long value, Map<String, Number> descriptions) {
        this.channelId = channelId;
        this.value = value;
        this.descriptions = descriptions;
    }

    public static ChannelRating forChannel(ChannelId channelId) {
        return new ChannelRating(channelId).withDescription("rating", 0);
    }

    public ChannelRating combine(ChannelRating other) {
        throwIfDifferentChannel(other);
        long newRating = value + other.value;
        Map<String, Number> combinedDescriptions = new LinkedHashMap<>();
        combinedDescriptions.putAll(descriptions);
        combinedDescriptions.putAll(other.descriptions);
        combinedDescriptions.put(channelId + " rating", newRating);
        return new ChannelRating(channelId, newRating, combinedDescriptions);
    }

    public ChannelRating addValueWithDescription(long value, String description) {
        ChannelRating newRating = new ChannelRating(channelId, value, Map.of(channelId + " " + description, value));
        return combine(newRating);
    }

    public ChannelRating forDays(long days) {
        double factor = 1.0 / days;
        long newValue = (long) (value * factor);
        return new ChannelRating(channelId, newValue, descriptions)
                .withDescription("scaled by days", factor);
    }

    public ChannelRating forAverageLocalBalance(CoinsAndDuration averageLocalBalance) {
        double millionSatoshis = averageLocalBalance.coins().milliSatoshis() / 1_000_000_000.0;
        double factor = 1.0 / millionSatoshis;
        long newValue = (long) (value * factor);
        long days = averageLocalBalance.duration().toDays();
        String description = "scaled by liquidity (%.1f million sats for %s days)".formatted(millionSatoshis, days);
        return new ChannelRating(channelId, newValue, descriptions)
                .withDescription(description, factor);
    }

    @Override
    public long getValue() {
        return value;
    }

    @Override
    public Map<String, Number> getDescriptions() {
        return descriptions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ChannelRating that = (ChannelRating) other;
        return value == that.value
               && Objects.equals(channelId, that.channelId)
               && Objects.equals(descriptions, that.descriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, value, descriptions);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ChannelRating withDescription(String description, Number value) {
        return combine(new ChannelRating(channelId, 0, Map.of(channelId + " " + description, value)));
    }

    private void throwIfDifferentChannel(ChannelRating other) {
        if (channelId.equals(other.channelId)) {
            return;
        }
        String message = "Ratings created for different channels (%s and %s)".formatted(channelId, other.channelId);
        throw new IllegalArgumentException(message);
    }
}
