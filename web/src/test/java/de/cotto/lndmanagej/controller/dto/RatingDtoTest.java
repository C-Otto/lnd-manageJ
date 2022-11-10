package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelRatingFixtures;
import de.cotto.lndmanagej.model.Rating;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class RatingDtoTest {
    @Test
    void empty() {
        assertThat(RatingDto.EMPTY)
                .isEqualTo(new RatingDto(-1, "Unable to compute rating", Map.of()));
    }

    @Test
    void fromModel() {
        Map<String, String> expectedDescriptions = Map.of(
                CHANNEL_ID + " raw rating", "1",
                CHANNEL_ID + " something", "1"
        );
        assertThat(RatingDto.fromModel(ChannelRatingFixtures.ratingWithValue(1)))
                .isEqualTo(new RatingDto(1, "", expectedDescriptions));
    }

    @Test
    void fromModel_with_descriptions() {
        Map<String, String> expectedDescriptions = Map.of(
                CHANNEL_ID + " a", "1",
                CHANNEL_ID + " b", "2",
                CHANNEL_ID + " raw rating", "4",
                CHANNEL_ID + " something", "1"
        );
        Rating rating = ChannelRatingFixtures.ratingWithValue(1)
                .addValueWithDescription(1, "a")
                .addValueWithDescription(2, "b");
        assertThat(RatingDto.fromModel(rating)).isEqualTo(new RatingDto(4, "", expectedDescriptions));
    }
}
