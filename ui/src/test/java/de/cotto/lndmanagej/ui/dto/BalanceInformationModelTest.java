package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.BALANCE_INFORMATION_MODEL;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.LOW_LOCAL_MODEL;
import static de.cotto.lndmanagej.ui.dto.BalanceInformationModelFixture.LOW_REMOTE_MODEL;
import static org.assertj.core.api.Assertions.assertThat;

class BalanceInformationModelTest {

    @Test
    void routableCapacity() {
        assertThat(BALANCE_INFORMATION_MODEL.getRoutableCapacity()).isEqualTo(1123L);
    }

    @Test
    void inboundPercentage() {
        assertThat(BALANCE_INFORMATION_MODEL.getInboundPercentage()).isEqualTo(10.952_804_986_642_917);
    }

    @Test
    void outboundPercentage() {
        assertThat(BALANCE_INFORMATION_MODEL.getOutboundPercentage()).isEqualTo(100 - 10.952_804_986_642_917);
    }

    @Test
    void inboundPercentageLabel() {
        BalanceInformationModel balance = new BalanceInformationModel(900, 0, 900, 100, 0, 100);
        assertThat(balance.getInboundPercentageLabel()).isEqualTo("10%");
    }

    @Test
    void outboundPercentageLabel() {
        BalanceInformationModel balance = new BalanceInformationModel(100, 0, 100, 900, 0, 900);
        assertThat(balance.getOutboundPercentageLabel()).isEqualTo("10%");
    }

    @Test
    void inboundPercentageLabel_belowTen_empty() {
        assertThat(LOW_REMOTE_MODEL.getInboundPercentageLabel()).isEmpty();
    }

    @Test
    void outboundPercentageLabel_belowTen_empty() {
        assertThat(LOW_LOCAL_MODEL.getOutboundPercentageLabel()).isEmpty();
    }
}