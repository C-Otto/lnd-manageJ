package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_BALANCE_FLUCTUATION_WARNING;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelWarningsTest {
    @Test
    void warnings() {
        assertThat(CHANNEL_WARNINGS.warnings()).containsExactlyInAnyOrder(
                CHANNEL_BALANCE_FLUCTUATION_WARNING
        );
    }

    @Test
    void descriptions() {
        assertThat(CHANNEL_WARNINGS.descriptions()).containsExactlyInAnyOrder(
                CHANNEL_BALANCE_FLUCTUATION_WARNING.description()
        );
    }

    @Test
    void none() {
        assertThat(ChannelWarnings.NONE).isEqualTo(new ChannelWarnings(Set.of()));
    }
}
