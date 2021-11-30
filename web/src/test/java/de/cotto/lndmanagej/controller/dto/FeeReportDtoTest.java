package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeReportDtoTest {

    private static final FeeReportDto FEE_REPORT_DTO =
            new FeeReportDto(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Test
    void earned() {
        assertThat(FEE_REPORT_DTO.earned()).isEqualTo("1234");
    }

    @Test
    void sourced() {
        assertThat(FEE_REPORT_DTO.sourced()).isEqualTo("567");
    }
}