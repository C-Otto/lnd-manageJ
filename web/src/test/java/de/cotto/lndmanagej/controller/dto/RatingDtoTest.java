package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Rating;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RatingDtoTest {
    @Test
    void fromModel_empty() {
        assertThat(RatingDto.fromModel(Rating.EMPTY)).isEqualTo(new RatingDto(-1, "Unable to compute rating"));
    }

    @Test
    void fromModel() {
        assertThat(RatingDto.fromModel(new Rating(1))).isEqualTo(new RatingDto(1, ""));
    }

    @Test
    void fromModel_with_details() {
        Map<String, String> expectedDetails = Map.of("a", "1", "b", "2");
        Rating rating = new Rating(1).withDescription("a", 1).withDescription("b", 2);
        assertThat(RatingDto.fromModel(rating)).isEqualTo(new RatingDto(1, "", expectedDetails));
    }
}
