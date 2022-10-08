package de.cotto.lndmanagej.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record Rating(Optional<Long> rating, Map<Object, Object> details) {
    public static final Rating EMPTY = new Rating(Optional.empty(), Map.of());

    public Rating(long rating) {
        this(rating, Map.of());
    }

    public Rating(long rating, Map<Object, Object> details) {
        this(Optional.of(rating), details);
    }

    public Rating(long rating, Object key, Object value) {
        this(Optional.of(rating), Map.of(key, value));
    }

    public Rating add(Rating other) {
        Long thisRating = rating.orElse(null);
        if (thisRating == null) {
            return other;
        }
        Long otherRating = other.rating.orElse(null);
        if (otherRating == null) {
            return this;
        }
        Map<Object, Object> combinedDetails = new LinkedHashMap<>();
        combinedDetails.putAll(details);
        combinedDetails.putAll(other.details);
        return new Rating(thisRating + otherRating, combinedDetails);
    }

    public Rating addValueWithDetailKey(long value, Object key) {
        Rating newRating = new Rating(value, key, value);
        return add(newRating);
    }

    public Rating withDetail(Object key, Object value) {
        return this.add(new Rating(0L, key, value));
    }

    public Rating scaleBy(double factor, Object detailKey) {
        Rating withDetail = withDetail(detailKey, factor);
        long scaledValue = (long) (withDetail.getRating() * factor);
        return new Rating(scaledValue, withDetail.details);
    }

    public boolean isEmpty() {
        return rating.isEmpty();
    }

    public long getRating() {
        return rating.orElse(-1L);
    }
}
