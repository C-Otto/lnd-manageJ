package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyTest {
    @Test
    void feeRate() {
        assertThat(POLICY_1.feeRate()).isEqualTo(100L);
    }

    @Test
    void baseFee() {
        assertThat(POLICY_1.baseFee()).isEqualTo(Coins.ofMilliSatoshis(10));
    }

    @Test
    void enabled() {
        assertThat(POLICY_1.enabled()).isFalse();
    }

    @Test
    void unknown() {
        assertThat(Policy.UNKNOWN).isEqualTo(new Policy(0, Coins.NONE, false));
    }
}
