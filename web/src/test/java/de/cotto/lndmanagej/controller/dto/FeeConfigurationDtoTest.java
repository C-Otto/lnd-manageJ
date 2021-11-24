package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeConfigurationDtoTest {
    @Test
    void createFrom() {
        FeeConfigurationDto expected = new FeeConfigurationDto(1, 2, 3, 4);

        FeeConfiguration feeConfiguration =
                new FeeConfiguration(1, Coins.ofMilliSatoshis(2), 3, Coins.ofMilliSatoshis(4));
        FeeConfigurationDto dto = FeeConfigurationDto.createFrom(feeConfiguration);

        assertThat(dto).isEqualTo(expected);
    }
}