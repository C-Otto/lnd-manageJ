package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Rating;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public record RatingDto(long rating, String message, Map<String, String> descriptions) {
    public static final RatingDto EMPTY = new RatingDto(-1, "Unable to compute rating", Map.of());

    public static RatingDto fromModel(Rating rating) {
        return new RatingDto(rating.getValue(), "", toStringDetails(rating.getDescriptions()));
    }

    private static Map<String, String> toStringDetails(Map<String, Number> descriptions) {
        return descriptions.entrySet().stream().collect(toMap(
                Map.Entry::getKey,
                e -> String.valueOf(e.getValue())
        ));
    }
}
