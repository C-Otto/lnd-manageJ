package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeReportDtoTest {

    private static final FeeReportDto FEE_REPORT_DTO =
            new FeeReportDto(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Test
    void earned() {
        assertThat(FEE_REPORT_DTO.earnedMilliSat()).isEqualTo("1234");
    }

    @Test
    void sourced() {
        assertThat(FEE_REPORT_DTO.sourcedMilliSat()).isEqualTo("567");
    }

    @Test
    void createFromModel() {
        assertThat(FeeReportDto.createFromModel(new FeeReport(Coins.ofSatoshis(1), Coins.ofSatoshis(2))))
                .isEqualTo(new FeeReportDto("1000", "2000"));
    }
}