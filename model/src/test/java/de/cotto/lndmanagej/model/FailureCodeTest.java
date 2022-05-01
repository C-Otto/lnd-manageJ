package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FailureCode.CHANNEL_DISABLED;
import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static de.cotto.lndmanagej.model.FailureCode.UNKNOWN_NEXT_PEER;
import static org.assertj.core.api.Assertions.assertThat;

class FailureCodeTest {
    @Test
    void channelDisabled() {
        assertThat(CHANNEL_DISABLED.code()).isEqualTo(14);
    }

    @Test
    void temporaryChannelFailure() {
        assertThat(TEMPORARY_CHANNEL_FAILURE.code()).isEqualTo(15);
    }

    @Test
    void unknownNextPeer() {
        assertThat(UNKNOWN_NEXT_PEER.code()).isEqualTo(18);
    }
}
