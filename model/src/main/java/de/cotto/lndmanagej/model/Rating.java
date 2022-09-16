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

    public boolean isEmpty() {
        return rating.isEmpty();
    }

    public long getRating() {
        return rating.orElse(-1L);
    }
}
