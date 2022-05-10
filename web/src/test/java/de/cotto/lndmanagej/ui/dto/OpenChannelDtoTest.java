package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

class OpenChannelDtoTest {

    @Test
    void getLocalBalancePercentForPerfectlyBalancedChannel() {
        assertThat(ACINQ.getOutboundPercentage()).isCloseTo(50, withPercentage(1));
    }
}
