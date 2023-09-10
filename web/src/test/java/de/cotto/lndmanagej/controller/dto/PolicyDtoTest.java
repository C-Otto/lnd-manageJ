package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_WITH_BASE_FEE;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyDtoTest {
    @Test
    void createFromModel_disabled() {
        PolicyDto expected = new PolicyDto(200, "0", false, 40, "0", "0");
        PolicyDto dto = PolicyDto.createFromModel(POLICY_DISABLED);
        assertThat(dto).isEqualTo(expected);
    }

    @Test
    void createFromModel_with_base_fee() {
        PolicyDto expected = new PolicyDto(200, "10", true, 40, "159000", "10000000");
        PolicyDto dto = PolicyDto.createFromModel(POLICY_WITH_BASE_FEE);
        assertThat(dto).isEqualTo(expected);
    }
}
