package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.ACINQ;
import static org.junit.jupiter.api.Assertions.*;

class OpenChannelDtoTest {

    @Test
    void getLocalBalancePercentForPerfectlyBalancedChannel() {
        assertEquals(50, ACINQ.getOutboundPercentage(), 0.01);
    }
}