package de.cotto.lndmanagej.model.warnings;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_BALANCE_FLUCTUATION_WARNING;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelBalanceFluctuationWarningTest {
    @Test
    void minLocalBalancePercentage() {
        assertThat(CHANNEL_BALANCE_FLUCTUATION_WARNING.minLocalBalancePercentage()).isEqualTo(2);
    }

    @Test
    void maxLocalBalancePercentage() {
        assertThat(CHANNEL_BALANCE_FLUCTUATION_WARNING.maxLocalBalancePercentage()).isEqualTo(97);
    }

    @Test
    void days() {
        assertThat(CHANNEL_BALANCE_FLUCTUATION_WARNING.days()).isEqualTo(7);
    }

    @Test
    void description() {
        assertThat(CHANNEL_BALANCE_FLUCTUATION_WARNING.description())
                .isEqualTo("Channel balance ranged from 2% to 97% in the past 7 days");
    }
}