package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyDtoTest {
    @Test
    void createFrom() {
        PolicyDto expected = new PolicyDto(100, 10, false);
        PolicyDto dto = PolicyDto.createFrom(POLICY_1);
        assertThat(dto).isEqualTo(expected);
    }
}