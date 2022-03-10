package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static org.assertj.core.api.Assertions.assertThat;

class RebalanceReportDtoTest {

    private static final RebalanceReportDto REBALANCE_REPORT_DTO = new RebalanceReportDto(
            Coins.ofMilliSatoshis(1_234),
            Coins.ofSatoshis(9_000),
            Coins.ofMilliSatoshis(567),
            Coins.ofSatoshis(1_000),
            Coins.ofSatoshis(100),
            Coins.ofSatoshis(200)
    );

    @Test
    void sourceCosts() {
        assertThat(REBALANCE_REPORT_DTO.sourceCostsMilliSat()).isEqualTo("1234");
    }

    @Test
    void sourceAmount() {
        assertThat(REBALANCE_REPORT_DTO.sourceAmountMilliSat()).isEqualTo("9000000");
    }

    @Test
    void targetCosts() {
        assertThat(REBALANCE_REPORT_DTO.targetCostsMilliSat()).isEqualTo("567");
    }

    @Test
    void targetAmount() {
        assertThat(REBALANCE_REPORT_DTO.targetAmountMilliSat()).isEqualTo("1000000");
    }

    @Test
    void supportAsSourceAmount() {
        assertThat(REBALANCE_REPORT_DTO.supportAsSourceAmountMilliSat()).isEqualTo("100000");
    }

    @Test
    void supportAsTargetAmount() {
        assertThat(REBALANCE_REPORT_DTO.supportAsTargetAmountMilliSat()).isEqualTo("200000");
    }

    @Test
    void createFromModel() {
        assertThat(RebalanceReportDto.createFromModel(REBALANCE_REPORT)).isEqualTo(
                new RebalanceReportDto("1000000", "665000", "2000000", "991000", "100000", "200000")
        );
    }
}