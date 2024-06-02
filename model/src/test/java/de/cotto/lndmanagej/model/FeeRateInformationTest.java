package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeRateInformationTest {
    @Test
    void fromPolicies() {
        Policy local = new Policy(
                123,
                Coins.ofMilliSatoshis(456),
                789,
                Coins.ofMilliSatoshis(1),
                true,
                1,
                Coins.NONE,
                Coins.NONE
        );
        Policy remote = new Policy(
                100,
                Coins.ofMilliSatoshis(200),
                300,
                Coins.ofMilliSatoshis(400),
                true,
                1,
                Coins.NONE,
                Coins.NONE
        );
        PoliciesForLocalChannel policies = new PoliciesForLocalChannel(local, remote);

        FeeRateInformation expected = new FeeRateInformation(
            Coins.ofMilliSatoshis(456),
                123,
                Coins.ofMilliSatoshis(1),
                789,
                Coins.ofMilliSatoshis(200),
                100,
                Coins.ofMilliSatoshis(400),
                300
        );
        assertThat(FeeRateInformation.fromPolicies(policies)).isEqualTo(expected);
    }
}
