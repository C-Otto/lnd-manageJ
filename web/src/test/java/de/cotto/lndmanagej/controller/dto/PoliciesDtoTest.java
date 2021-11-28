package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static org.assertj.core.api.Assertions.assertThat;

class PoliciesDtoTest {
    @Test
    void createFrom() {
        PoliciesDto expected = new PoliciesDto(PolicyDto.createFrom(POLICY_1), PolicyDto.createFrom(POLICY_2));

        PoliciesDto dto = PoliciesDto.createFrom(POLICIES);

        assertThat(dto).isEqualTo(expected);
    }
}