package de.cotto.lndmanagej.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Rating {
    public static final Rating EMPTY = new Rating(0, Map.of());

    private final long value;
    private final Map<String, Number> descriptions;

    public Rating(long value, Map<String, Number> descriptions) {
        this.value = value;
        this.descriptions = descriptions;
    }

    public Rating(long value) {
        this(value, Map.of());
    }

    public Rating combine(Rating other) {
        Map<String, Number> combinedDetails = new LinkedHashMap<>();
        combinedDetails.putAll(descriptions);
        combinedDetails.putAll(other.descriptions);
        return new Rating(value + other.value, combinedDetails);
    }

    public Rating addValueWithDescription(long value, String description) {
        Rating newRating = new Rating(value, Map.of(description, value));
        return combine(newRating);
    }

    public Rating withDescription(String description, Number value) {
        return combine(new Rating(0, Map.of(description, value)));
    }

    public Rating forDays(long days, ChannelId channelId) {
        double factor = 1.0 / days;
        long newValue = (long) (value * factor);
        return new Rating(newValue, descriptions)
                .withDescription(channelId + " scaled by days", factor);
    }

    public Rating forAverageLocalBalance(Coins averageLocalBalance, ChannelId channelId) {
        double millionSatoshis = averageLocalBalance.milliSatoshis() / 1_000_000_000.0;
        double factor = 1.0 / millionSatoshis;
        long newValue = (long) (value * factor);
        return new Rating(newValue, descriptions)
                .withDescription(channelId + " scaled by liquidity", factor);
    }

    public boolean isEmpty() {
        return equals(EMPTY);
    }

    public long getValue() {
        return value;
    }

    public Map<String, Number> getDescriptions() {
        return descriptions;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        var that = (Rating) other;
        return this.value == that.value
               && Objects.equals(this.descriptions, that.descriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, descriptions);
    }
}
