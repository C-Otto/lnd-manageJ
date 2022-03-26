package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyDtoTest {
    @Test
    void createFromModel() {
        PolicyDto expected = new PolicyDto(100, 10, false);
        PolicyDto dto = PolicyDto.createFromModel(POLICY_DISABLED);
        assertThat(dto).isEqualTo(expected);
    }
}
