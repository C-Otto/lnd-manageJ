package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.FeeConfigurationFixtures.FEE_CONFIGURATION;
import static org.assertj.core.api.Assertions.assertThat;

class FeeConfigurationTest {
    @Test
    void outgoingFeeRate() {
        assertThat(FEE_CONFIGURATION.outgoingFeeRate()).isEqualTo(1);
    }

    @Test
    void outgoingBaseFee() {
        assertThat(FEE_CONFIGURATION.outgoingBaseFee()).isEqualTo(Coins.ofMilliSatoshis(2));
    }

    @Test
    void incomingFeeRate() {
        assertThat(FEE_CONFIGURATION.incomingFeeRate()).isEqualTo(3);
    }

    @Test
    void incomingBaseFee() {
        assertThat(FEE_CONFIGURATION.incomingBaseFee()).isEqualTo(Coins.ofMilliSatoshis(4));
    }
}