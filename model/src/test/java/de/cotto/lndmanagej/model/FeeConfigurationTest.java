package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeeConfigurationTest {
    private final FeeConfiguration feeConfiguration =
            new FeeConfiguration(10, Coins.ofMilliSatoshis(20), 30, Coins.ofMilliSatoshis(40));

    @Test
    void outgoingFeeRate() {
        assertThat(feeConfiguration.outgoingFeeRate()).isEqualTo(10L);
    }

    @Test
    void outgoingBaseFee() {
        assertThat(feeConfiguration.outgoingBaseFee()).isEqualTo(Coins.ofMilliSatoshis(20));
    }

    @Test
    void incomingFeeRate() {
        assertThat(feeConfiguration.incomingFeeRate()).isEqualTo(30L);
    }

    @Test
    void incomingBaseFee() {
        assertThat(feeConfiguration.incomingBaseFee()).isEqualTo(Coins.ofMilliSatoshis(40));
    }
}