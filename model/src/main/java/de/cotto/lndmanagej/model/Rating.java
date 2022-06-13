package de.cotto.lndmanagej.model;

import java.util.Optional;

public record Rating(Optional<Long> rating) {
    public static final Rating EMPTY = new Rating(Optional.empty());

    public Rating(long rating) {
        this(Optional.of(rating));
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
        return new Rating(thisRating + otherRating);
    }

    public boolean isEmpty() {
        return rating.isEmpty();
    }

    public long getRating() {
        return rating.orElse(-1L);
    }
}
