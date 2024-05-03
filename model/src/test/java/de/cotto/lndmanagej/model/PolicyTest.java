package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_WITH_BASE_FEE;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_WITH_NEGATIVE_INBOUND_FEES;
import static org.assertj.core.api.Assertions.assertThat;

class PolicyTest {
    @Test
    void feeRate() {
        assertThat(POLICY_1.feeRate()).isEqualTo(200L);
    }

    @Test
    void baseFee() {
        assertThat(POLICY_WITH_BASE_FEE.baseFee()).isEqualTo(Coins.ofMilliSatoshis(10));
    }

    @Test
    void inboundFeeRate() {
        assertThat(POLICY_WITH_NEGATIVE_INBOUND_FEES.inboundFeeRate()).isEqualTo(-100L);
    }

    @Test
    void inboundBaseFee() {
        assertThat(POLICY_WITH_NEGATIVE_INBOUND_FEES.inboundBaseFee()).isEqualTo(Coins.ofMilliSatoshis(-1));
    }

    @Test
    void enabled() {
        assertThat(POLICY_1.enabled()).isTrue();
        assertThat(POLICY_1.disabled()).isFalse();
    }

    @Test
    void disabled() {
        assertThat(POLICY_DISABLED.enabled()).isFalse();
        assertThat(POLICY_DISABLED.disabled()).isTrue();
    }

    @Test
    void minHtlc() {
        assertThat(POLICY_1.minHtlc()).isEqualTo(Coins.ofSatoshis(159));
    }

    @Test
    void maxHtlc() {
        assertThat(POLICY_1.maxHtlc()).isEqualTo(Coins.ofSatoshis(10_000));
    }

    @Test
    void unknown() {
        assertThat(Policy.UNKNOWN).isEqualTo(new Policy(0, Coins.NONE, false, 0, Coins.NONE, Coins.NONE));
    }
}
