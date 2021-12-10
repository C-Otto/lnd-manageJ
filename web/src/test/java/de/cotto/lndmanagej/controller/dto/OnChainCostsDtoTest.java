package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static org.assertj.core.api.Assertions.assertThat;

class OnChainCostsDtoTest {
    @Test
    void createFromModel() {
        assertThat(OnChainCostsDto.createFromModel(ON_CHAIN_COSTS))
                .isEqualTo(new OnChainCostsDto("1000", "2000"));
    }
}