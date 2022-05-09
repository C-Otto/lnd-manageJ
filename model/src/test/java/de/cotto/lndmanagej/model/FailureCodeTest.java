package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FailureCode.CHANNEL_DISABLED;
import static de.cotto.lndmanagej.model.FailureCode.FEE_INSUFFICIENT;
import static de.cotto.lndmanagej.model.FailureCode.INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS;
import static de.cotto.lndmanagej.model.FailureCode.MPP_TIMEOUT;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_NEXT_PEER;
import static org.assertj.core.api.Assertions.assertThat;

class FailureCodeTest {
    @Test
    void getFor_unknown() {
        assertThat(FailureCode.getFor(99)).isEqualTo(UNKNOWN_FAILURE);
    }

    @Test
    void incorrectOrUnknownPaymentDetails() {
        assertThat(FailureCode.getFor(1)).isEqualTo(INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS);
    }

    @Test
    void channelDisabled() {
        assertThat(FailureCode.getFor(14)).isEqualTo(CHANNEL_DISABLED);
    }

    @Test
    void feeInsufficient() {
        assertThat(FailureCode.getFor(12)).isEqualTo(FEE_INSUFFICIENT);
    }

    @Test
    void temporaryChannelFailure() {
        assertThat(FailureCode.getFor(15)).isEqualTo(TEMPORARY_CHANNEL_FAILURE);
    }

    @Test
    void unknownNextPeer() {
        assertThat(FailureCode.getFor(18)).isEqualTo(UNKNOWN_NEXT_PEER);
    }

    @Test
    void mppTimeout() {
        assertThat(FailureCode.getFor(23)).isEqualTo(MPP_TIMEOUT);
    }
}
