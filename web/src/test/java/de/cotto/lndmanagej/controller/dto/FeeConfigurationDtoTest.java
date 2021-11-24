package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FeeConfigurationFixtures.FEE_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;

class FeeConfigurationDtoTest {
    @Test
    void createFrom() {
        FeeConfigurationDto expected = new FeeConfigurationDto(1, 2, 3, 4);

        FeeConfigurationDto dto = FeeConfigurationDto.createFrom(FEE_CONFIGURATION);

        assertThat(dto).isEqualTo(expected);
    }
}