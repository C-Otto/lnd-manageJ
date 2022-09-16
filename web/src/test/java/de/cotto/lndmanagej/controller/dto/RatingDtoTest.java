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
        Map<Object, Object> details = Map.of("a", "b", "c", "d");
        Map<String, String> expectedDetails = Map.of("a", "b", "c", "d");
        Rating rating = new Rating(1, details);
        assertThat(RatingDto.fromModel(rating)).isEqualTo(new RatingDto(1, "", expectedDetails));
    }
}
