package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static org.assertj.core.api.Assertions.assertThat;

class PoliciesTest {
    @Test
    void local() {
        assertThat(POLICIES.local()).isEqualTo(POLICY_1);
    }

    @Test
    void remote() {
        assertThat(POLICIES.remote()).isEqualTo(POLICY_2);
    }

    @Test
    void unknown() {
        assertThat(Policies.UNKNOWN).isEqualTo(new Policies(Policy.UNKNOWN, Policy.UNKNOWN));
    }
}
