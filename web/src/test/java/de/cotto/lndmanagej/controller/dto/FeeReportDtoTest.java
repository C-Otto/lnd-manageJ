package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeReportDtoTest {
    @Test
    void earned() {
        assertThat(new FeeReportDto(Coins.ofMilliSatoshis(1_234)).earned()).isEqualTo("1234");
    }
}