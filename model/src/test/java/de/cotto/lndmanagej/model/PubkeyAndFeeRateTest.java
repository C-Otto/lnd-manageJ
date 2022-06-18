package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;

class PubkeyAndFeeRateTest {

    @Test
    void pubkey() {
        assertThat(new PubkeyAndFeeRate(PUBKEY, 123).pubkey()).isEqualTo(PUBKEY);
    }

    @Test
    void feeRate() {
        assertThat(new PubkeyAndFeeRate(PUBKEY, 123).feeRate()).isEqualTo(123);
    }
}