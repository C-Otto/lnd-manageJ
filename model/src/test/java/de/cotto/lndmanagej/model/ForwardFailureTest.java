package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.ForwardFailureFixtures.FORWARD_FAILURE;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;

class ForwardFailureTest {
    @Test
    void htlcDetails() {
        assertThat(FORWARD_FAILURE.htlcDetails()).isEqualTo(HTLC_DETAILS);
    }

    @Test
    void forwardAttempt() {
        assertThat(FORWARD_FAILURE.forwardAttempt()).isEqualTo(FORWARD_ATTEMPT);
    }
}