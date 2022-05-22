package de.cotto.lndmanagej.pickhardtpayments.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentOptionsTest {
    @Test
    void forFeeRateWeight() {
        assertThat(PaymentOptions.forFeeRateWeight(12))
                .isEqualTo(new PaymentOptions(12, Optional.empty(), true, Optional.empty()));
    }

    @Test
    void forFeeRateLimit() {
        assertThat(PaymentOptions.forFeeRateLimit(123))
                .isEqualTo(new PaymentOptions(0, Optional.of(123L), true, Optional.empty()));
    }

    @Test
    void forTopUp() {
        assertThat(PaymentOptions.forTopUp(123, PUBKEY))
                .isEqualTo(new PaymentOptions(10, Optional.of(123L), false, Optional.of(PUBKEY)));
    }

    @Test
    void defaultPaymentOptions() {
        assertThat(DEFAULT_PAYMENT_OPTIONS).isEqualTo(PaymentOptions.forFeeRateWeight(0));
    }
}
