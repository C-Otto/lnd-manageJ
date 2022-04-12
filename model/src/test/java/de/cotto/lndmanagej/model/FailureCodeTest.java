package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FailureCode.TEMPORARY_CHANNEL_FAILURE;
import static org.assertj.core.api.Assertions.assertThat;

class FailureCodeTest {
    @Test
    void temporaryChannelFailure() {
        assertThat(TEMPORARY_CHANNEL_FAILURE.code()).isEqualTo(15);
    }
}
