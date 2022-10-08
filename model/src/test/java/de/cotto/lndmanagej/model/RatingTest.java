package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class RatingTest {
    @Test
    void empty() {
        assertThat(Rating.EMPTY).isEqualTo(new Rating(Optional.empty(), Map.of()));
    }

    @Test
    void isEmpty() {
        assertThat(Rating.EMPTY.isEmpty()).isTrue();
        assertThat(new Rating(1).isEmpty()).isFalse();
    }

    @Test
    void getRating() {
        assertThat(Rating.EMPTY.getRating()).isEqualTo(-1);
        assertThat(new Rating(1).getRating()).isEqualTo(1);
    }

    @Test
    void add_empty_and_empty() {
        assertThat(Rating.EMPTY.add(Rating.EMPTY)).isEqualTo(Rating.EMPTY);
    }

    @Test
    void add_empty_and_something() {
        Rating something = new Rating(1);
        assertThat(Rating.EMPTY.add(something)).isEqualTo(something);
    }

    @Test
    void add_something_and_empty() {
        Rating something = new Rating(1);
        assertThat(something.add(Rating.EMPTY)).isEqualTo(something);
    }

    @Test
    void add_something_and_something() {
        Rating something = new Rating(1);
        assertThat(something.add(something)).isEqualTo(new Rating(2));
    }

    @Test
    void rating_with_detail_has_rating() {
        Rating withDetails = new Rating(123, "a", "b");
        assertThat(withDetails.rating()).contains(123L);
    }

    @Test
    void rating_with_details_has_rating() {
        Rating withDetails = new Rating(123, Map.of("a", "b", "c", "d"));
        assertThat(withDetails.rating()).contains(123L);
    }

    @Test
    void rating_with_detail_has_detail() {
        Rating withDetails = new Rating(123, "a", "b");
        assertThat(withDetails.details()).contains(entry("a", "b"));
    }

    @Test
    void rating_with_details_has_details() {
        Rating withDetails = new Rating(123, Map.of("a", "b"));
        assertThat(withDetails.details()).contains(entry("a", "b"));
    }

    @Test
    void add_ratings_with_details() {
        Rating withDetails1 = new Rating(1, Map.of("a", "b", "c", "d"));
        Rating withDetails2 = new Rating(2, Map.of("e", "f", "g", "h"));
        assertThat(withDetails1.add(withDetails2).details()).isEqualTo(Map.of("a", "b", "c", "d", "e", "f", "g", "h"));
    }

    @Test
    void addValueWithDetailKey_adds_value() {
        Rating rating = new Rating(1, "a", "b");
        assertThat(rating.addValueWithDetailKey(123, "c").rating()).contains(124L);
    }

    @Test
    void addValueWithDetailKey_has_details() {
        Rating rating = new Rating(1, "a", "b");
        assertThat(rating.addValueWithDetailKey(123, "c").details()).isEqualTo(Map.of("a", "b", "c", 123L));
    }

    @Test
    void withDetail_keeps_value() {
        Rating rating = new Rating(1, "a", "b");
        assertThat(rating.withDetail("c", "d").rating()).contains(1L);
    }

    @Test
    void withDetail_has_details() {
        Rating rating = new Rating(1, "a", "b");
        assertThat(rating.withDetail("c", "d").details()).isEqualTo(Map.of("a", "b", "c", "d"));
    }

    @Test
    void scaleBy_scales_value() {
        Rating rating = new Rating(100, "a", "b");
        assertThat(rating.scaleBy(0.33, "c").rating()).contains(33L);
    }

    @Test
    void scaleBy_has_details() {
        Rating rating = new Rating(1, "a", "b");
        assertThat(rating.scaleBy(0.33, "c").details()).isEqualTo(Map.of("a", "b", "c", 0.33));
    }
}
