package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_RATING_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class NodeRatingWarningTest {
    @Test
    void rating() {
        assertThat(NODE_RATING_WARNING.rating()).isEqualTo(1_234_567);
    }

    @Test
    void threshold() {
        assertThat(NODE_RATING_WARNING.threshold()).isEqualTo(2_345_678);
    }

    @Test
    void description() {
        assertThat(NODE_RATING_WARNING.description()).isEqualTo("Rating of 1,234,567 is below threshold of 2,345,678");
    }
}
