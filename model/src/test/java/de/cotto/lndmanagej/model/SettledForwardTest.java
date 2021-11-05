package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static de.cotto.lndmanagej.model.SettledForwardFixtures.SETTLED_FORWARD;
import static org.assertj.core.api.Assertions.assertThat;

class SettledForwardTest {
    @Test
    void htlcDetails() {
        assertThat(SETTLED_FORWARD.htlcDetails()).isEqualTo(HTLC_DETAILS);
    }

    @Test
    void forwardAttempt() {
        assertThat(SETTLED_FORWARD.forwardAttempt()).isEqualTo(FORWARD_ATTEMPT);
    }
}