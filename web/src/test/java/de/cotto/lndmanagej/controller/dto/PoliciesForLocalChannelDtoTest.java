package de.cotto.lndmanagej.controller.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static org.assertj.core.api.Assertions.assertThat;

class PoliciesForLocalChannelDtoTest {
    @Test
    void createFromModel() {
        PoliciesDto expected =
                new PoliciesDto(PolicyDto.createFromModel(POLICY_1), PolicyDto.createFromModel(POLICY_2));
        PoliciesDto dto = PoliciesDto.createFromModel(POLICIES_FOR_LOCAL_CHANNEL);
        assertThat(dto).isEqualTo(expected);
    }
}
