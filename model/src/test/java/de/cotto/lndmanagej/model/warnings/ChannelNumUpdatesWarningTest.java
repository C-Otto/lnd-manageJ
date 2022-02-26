package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_NUM_UPDATES_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelNumUpdatesWarningTest {
    @Test
    void numUpdates() {
        assertThat(CHANNEL_NUM_UPDATES_WARNING.numUpdates()).isEqualTo(101_000L);
    }

    @Test
    void description() {
        assertThat(CHANNEL_NUM_UPDATES_WARNING.description())
                .isEqualTo("Channel has accumulated 101,000 updates");
    }
}