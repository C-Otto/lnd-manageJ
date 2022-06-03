package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Rating;

public record RatingDto(long rating, String message) {
    public static RatingDto fromModel(Rating rating) {
        if (rating.isEmpty()) {
            return new RatingDto(rating.getRating(), "Unable to compute rating");
        }
        return new RatingDto(rating.getRating(), "");
    }
}
