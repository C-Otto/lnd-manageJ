package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;

class RatingTest {
    @Test
    void empty() {
        assertThat(Rating.EMPTY).isEqualTo(new Rating(0, Map.of()));
    }

    @Test
    void isEmpty() {
        assertThat(Rating.EMPTY.isEmpty()).isTrue();
        Rating ratingWithDescription = Rating.EMPTY.withDescription("a", 1);
        assertThat(ratingWithDescription.isEmpty()).isFalse();
    }

    @Test
    void value() {
        assertThat(Rating.EMPTY.getValue()).isEqualTo(0);
        assertThat(new Rating(1).getValue()).isEqualTo(1);
    }

    @Test
    void combine_empty_and_empty() {
        assertThat(Rating.EMPTY.combine(Rating.EMPTY)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void combine_empty_and_something() {
        Rating something = new Rating(1);
        assertThat(Rating.EMPTY.combine(something)).isEqualTo(something);
    }

    @Test
    void combine_something_and_empty() {
        Rating something = new Rating(1);
        assertThat(something.combine(Rating.EMPTY)).isEqualTo(something);
    }

    @Test
    void combine_something_and_something() {
        Rating something = new Rating(1);
        assertThat(something.combine(something)).isEqualTo(new Rating(2));
    }

    @Test
    void rating_created_with_description_has_rating() {
        Rating withDescription = new Rating(123, Map.of("a", 123));
        assertThat(withDescription.getValue()).isEqualTo(123L);
    }

    @Test
    void rating_with_added_description_keeps_rating() {
        Rating withDescription = new Rating(123).withDescription("a", 456);
        assertThat(withDescription.getValue()).isEqualTo(123L);
    }

    @Test
    void add_ratings_with_descriptions() {
        Rating withDescriptions1 = new Rating(1).withDescription("a", 456).withDescription("c", 789);
        Rating withDescriptions2 = new Rating(2).withDescription("e", 111).withDescription("g", 1.23);
        assertThat(withDescriptions1.combine(withDescriptions2).getDescriptions())
                .isEqualTo(Map.of("a", 456, "c", 789, "e", 111, "g", 1.23));
    }

    @Test
    void addValueWithDescription_adds_value() {
        Rating rating = new Rating(1).withDescription("a", 111);
        assertThat(rating.addValueWithDescription(123, "c").getValue()).isEqualTo(124L);
    }

    @Test
    void addValueWithDescription_has_descriptions() {
        Rating rating = new Rating(1).withDescription("a", 111);
        assertThat(rating.addValueWithDescription(123, "c").getDescriptions())
                .isEqualTo(Map.of("a", 111, "c", 123L));
    }

    @Test
    void withDescription_adds_description() {
        Rating rating = new Rating(1).withDescription("a", 111);
        assertThat(rating.withDescription("c", 222).getDescriptions())
                .isEqualTo(Map.of("a", 111, "c", 222));
    }

    @Test
    void forDays_scales_value() {
        Rating rating = new Rating(100).withDescription("a", 111);
        assertThat(rating.forDays(3, CHANNEL_ID).getValue())
                .isEqualTo(33L);
    }

    @Test
    void forDays_adds_description() {
        Rating rating = new Rating(100).withDescription("a", 111);
        Map<String, Number> expectedDescriptions = Map.of("a", 111, CHANNEL_ID + " scaled by days", 1.0 / 3);
        assertThat(rating.forDays(3, CHANNEL_ID).getDescriptions())
                .isEqualTo(expectedDescriptions);
    }

    @Test
    void forAverageLocalBalance_scales_value() {
        Rating rating = new Rating(100).withDescription("a", 111);
        assertThat(rating.forAverageLocalBalance(Coins.ofSatoshis(2_000_000), CHANNEL_ID).getValue())
                .isEqualTo(50);
    }

    @Test
    void forAverageLocalBalance_adds_description() {
        Rating rating = new Rating(100).withDescription("a", 111);
        Map<String, Number> expectedDescriptions = Map.of("a", 111, CHANNEL_ID + " scaled by liquidity", 2.0);
        assertThat(rating.forAverageLocalBalance(Coins.ofSatoshis(500_000), CHANNEL_ID).getDescriptions())
                .isEqualTo(expectedDescriptions);
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(Rating.class).verify();
    }
}
