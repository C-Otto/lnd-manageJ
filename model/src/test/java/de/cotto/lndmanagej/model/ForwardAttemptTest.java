package de.cotto.lndmanagej.model;

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
    void htlcDetails() {
        assertThat(FORWARD_ATTEMPT.htlcDetails()).isEqualTo(HTLC_DETAILS);
    }

    @Test
    void incomingTimelock() {
        assertThat(FORWARD_ATTEMPT.incomingTimelock()).isEqualTo(1);
    }

    @Test
    void outgoingTimelock() {
        assertThat(FORWARD_ATTEMPT.outgoingTimelock()).isEqualTo(2);
    }

    @Test
    void incomingAmount() {
        assertThat(FORWARD_ATTEMPT.incomingAmount()).isEqualTo(Coins.ofMilliSatoshis(100));
    }

    @Test
    void outgoingAmount() {
        assertThat(FORWARD_ATTEMPT.outgoingAmount()).isEqualTo(Coins.ofMilliSatoshis(200));
    }
}