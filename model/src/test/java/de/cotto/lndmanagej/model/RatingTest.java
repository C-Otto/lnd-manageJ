package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RatingTest {
    @Test
    void empty() {
        assertThat(Rating.EMPTY).isEqualTo(new Rating(Optional.empty()));
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
}
