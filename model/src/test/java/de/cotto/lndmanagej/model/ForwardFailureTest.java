package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ForwardFailureFixtures.FORWARD_FAILURE;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;

class ForwardFailureTest {
    @Test
    void testToString() {
        assertThat(FORWARD_FAILURE).hasToString(
                "ForwardFailure{" +
                        "htlcDetails=" + HTLC_DETAILS +
                        ", forwardAttempt=ForwardAttempt{" +
                        "htlcDetails=" + HTLC_DETAILS +
                        ", incomingTimelock=1" +
                        ", outgoingTimelock=2" +
                        ", incomingAmount=0.100" +
                        ", outgoingAmount=0.200" +
                        "}" +
                        "}"
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ForwardFailure.class).usingGetClass().verify();
    }
}