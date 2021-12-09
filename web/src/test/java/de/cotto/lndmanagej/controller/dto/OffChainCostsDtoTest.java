package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OffChainCostsDtoTest {
    @Test
    void converts_to_msat_strings() {
        OffChainCostsDto dto = new OffChainCostsDto(Coins.ofMilliSatoshis(1), Coins.ofMilliSatoshis(1_234));
        assertThat(dto.rebalanceSource()).isEqualTo("1");
        assertThat(dto.rebalanceTarget()).isEqualTo("1234");
    }
}