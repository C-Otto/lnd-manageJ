package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OffChainCostsFixtures.OFF_CHAIN_COSTS;
import static org.assertj.core.api.Assertions.assertThat;

class OffChainCostsDtoTest {
    @Test
    void converts_to_msat_strings() {
        OffChainCostsDto dto = new OffChainCostsDto(Coins.ofMilliSatoshis(1), Coins.ofMilliSatoshis(1_234));
        assertThat(dto.rebalanceSource()).isEqualTo("1");
        assertThat(dto.rebalanceTarget()).isEqualTo("1234");
    }

    @Test
    void createFromModel() {
        assertThat(OffChainCostsDto.createFromModel(OFF_CHAIN_COSTS))
                .isEqualTo(new OffChainCostsDto("1000000", "2000000"));
    }
}