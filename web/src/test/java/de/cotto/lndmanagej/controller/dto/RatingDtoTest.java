package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Rating;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatingDtoTest {
    @Test
    void fromModel_empty() {
        assertThat(RatingDto.fromModel(Rating.EMPTY)).isEqualTo(new RatingDto(0, "Unable to compute rating"));
    }

    @Test
    void fromModel() {
        assertThat(RatingDto.fromModel(new Rating(1))).isEqualTo(new RatingDto(1, ""));
    }
}
