package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static de.cotto.lndmanagej.model.SettledForwardFixtures.SETTLED_FORWARD;
import static org.assertj.core.api.Assertions.assertThat;

class SettledForwardTest {
    @Test
    void testToString() {
        assertThat(SETTLED_FORWARD).hasToString(
                "SettledForward{" +
                        "htlcDetails=" + HTLC_DETAILS +
                        ", attempt=" + FORWARD_ATTEMPT +
                        "}"
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(SettledForward.class).usingGetClass().verify();
    }
}