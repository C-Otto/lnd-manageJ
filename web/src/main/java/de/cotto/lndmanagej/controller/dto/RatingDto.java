package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Rating;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public record RatingDto(long rating, String message, Map<String, String> details) {
    public RatingDto(long rating, String message) {
        this(rating, message, Map.of());
    }

    public static RatingDto fromModel(Rating rating) {
        if (rating.isEmpty()) {
            return new RatingDto(rating.getRating(), "Unable to compute rating", Map.of());
        }
        return new RatingDto(rating.getRating(), "", toStringDetails(rating.details()));
    }

    private static Map<String, String> toStringDetails(Map<Object, Object> details) {
        return details.entrySet().stream().collect(toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())
        ));
    }
}
