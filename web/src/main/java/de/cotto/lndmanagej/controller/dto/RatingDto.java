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
            return new RatingDto(-1, "Unable to compute rating", Map.of());
        }
        return new RatingDto(rating.getValue(), "", toStringDetails(rating.getDescriptions()));
    }

    private static Map<String, String> toStringDetails(Map<String, Number> details) {
        return details.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> String.valueOf(e.getValue())
        ));
    }
}
