package de.cotto.lndmanagej.model;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ChannelRating implements Rating {
    private final ChannelId channelId;
    private final long value;
    private final CoinsAndDuration averageLocalBalance;
    private final Map<String, Number> descriptions;

    public ChannelRating(ChannelId channelId) {
        this(channelId, 0, new CoinsAndDuration(Coins.NONE, Duration.ZERO), Map.of());
    }

    private ChannelRating(
            ChannelId channelId,
            long value,
            CoinsAndDuration averageLocalBalance,
            Map<String, Number> descriptions
    ) {
        this.channelId = channelId;
        this.value = value;
        this.averageLocalBalance = averageLocalBalance;
        this.descriptions = descriptions;
    }

    public static ChannelRating forChannel(ChannelId channelId) {
        return new ChannelRating(channelId)
                .withDescription("raw rating", 0);
    }

    public ChannelRating combine(ChannelRating other) {
        throwIfDifferentChannel(other);
        long newRating = value + other.value;
        Map<String, Number> combinedDescriptions = new LinkedHashMap<>();
        combinedDescriptions.putAll(descriptions);
        combinedDescriptions.putAll(other.descriptions);
        combinedDescriptions.put(channelId + " raw rating", newRating);
        CoinsAndDuration combinedAverageLocalBalance;
        if (averageLocalBalance.duration().compareTo(other.averageLocalBalance.duration()) > 0) {
            combinedAverageLocalBalance = averageLocalBalance;
        } else {
            combinedAverageLocalBalance = other.averageLocalBalance;
        }
        if (combinedAverageLocalBalance.duration().toMinutes() > 0) {
            combinedDescriptions.put(
                    channelId + " rating scaled by local balance",
                    getValueWithBalanceScaling(newRating, combinedAverageLocalBalance)
            );
        }
        return new ChannelRating(channelId, newRating, combinedAverageLocalBalance, combinedDescriptions);
    }

    public ChannelRating addValueWithDescription(long value, String description) {
        ChannelRating newRating = new ChannelRating(
                channelId,
                value,
                averageLocalBalance,
                Map.of(channelId + " " + description, value)
        );
        return combine(newRating);
    }

    public ChannelRating forDays(long days) {
        double factor = 1.0 / days;
        long newValue = (long) (value * factor);
        return new ChannelRating(channelId, newValue, averageLocalBalance, descriptions)
                .withDescription("scaled by days", factor);
    }

    public ChannelRating forAverageLocalBalance(CoinsAndDuration averageLocalBalance) {
        double millionSatoshis = averageLocalBalance.coins().getMillionSatoshis();
        long days = averageLocalBalance.duration().toDays();
        String description = "average local balance (million sat) for %s days".formatted(days);
        return new ChannelRating(channelId, value, averageLocalBalance, descriptions)
                .withDescription(description, millionSatoshis);
    }

    public long getValueWithoutBalanceScaling() {
        return value;
    }

    @Override
    public long getValue() {
        return getValueWithBalanceScaling(value, averageLocalBalance);
    }

    private static long getValueWithBalanceScaling(long value, CoinsAndDuration averageLocalBalance) {
        if (averageLocalBalance.coins().isPositive()) {
            return (long) (value / averageLocalBalance.coins().getMillionSatoshis());
        }
        return value;
    }

    public Coins getAverageLocalBalanceScaledToMinutes(long minutes) {
        double millionSatoshis = averageLocalBalance.coins().milliSatoshis();
        double factor = 1.0 * averageLocalBalance.duration().toMinutes() / minutes;
        return Coins.ofMilliSatoshis((long) (millionSatoshis * factor));
    }

    @Override
    public Map<String, Number> getDescriptions() {
        return descriptions;
    }

    public CoinsAndDuration getAverageLocalBalance() {
        return averageLocalBalance;
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
               && Objects.equals(averageLocalBalance, that.averageLocalBalance)
               && Objects.equals(descriptions, that.descriptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, value, averageLocalBalance, descriptions);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private ChannelRating withDescription(String description, Number value) {
        ChannelRating update = new ChannelRating(
                channelId,
                0,
                averageLocalBalance,
                Map.of(channelId + " " + description, value)
        );
        return combine(update);
    }

    private void throwIfDifferentChannel(ChannelRating other) {
        if (channelId.equals(other.channelId)) {
            return;
        }
        String message = "Ratings created for different channels (%s and %s)".formatted(channelId, other.channelId);
        throw new IllegalArgumentException(message);
    }
}
