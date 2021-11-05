package de.cotto.lndmanagej.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ForwardAttemptFixtures.FORWARD_ATTEMPT;
import static de.cotto.lndmanagej.model.HtlcDetailsFixtures.HTLC_DETAILS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class ForwardAttemptTest {
    @Test
    void builder_without_arguments() {
        assertThatNullPointerException().isThrownBy(() ->
                ForwardAttempt.builder().build()
        );
    }

    @Test
    void builder_with_all_arguments() {
        ForwardAttempt forwardAttempt = ForwardAttempt.builder()
                .withHtlcDetails(HTLC_DETAILS)
                .withIncomingAmount(123)
                .withOutgoingAmount(456)
                .withIncomingTimelock(1)
                .withOutgoingTimelock(2)
                .build();
        assertThat(forwardAttempt).isNotNull();
    }

    @Test
    void testToString() {
        assertThat(FORWARD_ATTEMPT).hasToString(
                "ForwardAttempt{" +
                        "htlcDetails=" + HTLC_DETAILS +
                        ", incomingTimelock=1" +
                        ", outgoingTimelock=2" +
                        ", incomingAmount=0.100" +
                        ", outgoingAmount=0.200" +
                        "}"
        );
    }

    @Test
    void testEquals() {
        EqualsVerifier.forClass(ForwardAttempt.class).usingGetClass().verify();
    }
}