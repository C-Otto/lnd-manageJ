package de.cotto.lndmanagej.pickhardtpayments.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentOptionsTest {
    @Test
    void feeRateWeight() {
        assertThat(PaymentOptions.feeRateWeight(12)).isEqualTo(new PaymentOptions(12));
    }

    @Test
    void defaultPaymentOptions() {
        assertThat(DEFAULT_PAYMENT_OPTIONS).isEqualTo(PaymentOptions.feeRateWeight(0));
    }
}
