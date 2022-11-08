package de.cotto.lndmanagej.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record Rating(long value, Map<String, Number> descriptions) {
    public static final Rating EMPTY = new Rating(0, Map.of());

    public Rating(long value) {
        this(value, Map.of());
    }

    public Rating add(Rating other) {
        Map<String, Number> combinedDetails = new LinkedHashMap<>();
        combinedDetails.putAll(descriptions);
        combinedDetails.putAll(other.descriptions);
        return new Rating(value + other.value, combinedDetails);
    }

    public Rating addValueWithDescription(long value, String description) {
        Rating newRating = new Rating(value, Map.of(description, value));
        return add(newRating);
    }

    public Rating withDescription(String description, Number value) {
        return add(new Rating(0, Map.of(description, value)));
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
}
