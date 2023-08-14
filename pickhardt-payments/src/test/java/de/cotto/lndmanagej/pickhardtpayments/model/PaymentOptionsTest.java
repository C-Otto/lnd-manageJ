package de.cotto.lndmanagej.pickhardtpayments.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentOptionsTest {

    private static final int FEE_RATE_WEIGHT = 42;

    @Test
    void forFeeRateWeight() {
        assertThat(PaymentOptions.forFeeRateWeight(12)).isEqualTo(new PaymentOptions(
                Optional.of(12),
                Optional.empty(),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        ));
    }

    @Test
    void forFeeRateLimit() {
        assertThat(PaymentOptions.forFeeRateLimit(123)).isEqualTo(new PaymentOptions(
                Optional.of(0),
                Optional.of(123L),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        ));
    }

    @Test
    void forTopUp() {
        assertThat(PaymentOptions.forTopUp(FEE_RATE_WEIGHT, 123, 100, PUBKEY)).isEqualTo(new PaymentOptions(
                Optional.of(FEE_RATE_WEIGHT),
                Optional.of(123L),
                Optional.of(23L),
                false,
                Optional.of(PUBKEY),
                Optional.empty()
        ));
    }

    @Test
    void forTopUp_with_peer_for_first_hop() {
        assertThat(PaymentOptions.forTopUp(FEE_RATE_WEIGHT, 123, 100, PUBKEY, PUBKEY_2)).isEqualTo(new PaymentOptions(
                Optional.of(FEE_RATE_WEIGHT),
                Optional.of(123L),
                Optional.of(23L),
                false,
                Optional.of(PUBKEY),
                Optional.of(PUBKEY_2)
        ));
    }

    @Test
    void feeRateLimitFirstHops() {
        PaymentOptions paymentOptions = PaymentOptions.forTopUp(FEE_RATE_WEIGHT, 200, 30, PUBKEY);
        assertThat(paymentOptions.feeRateLimitExceptIncomingHops()).contains(170L);
    }

    @Test
    void defaultPaymentOptions() {
        assertThat(DEFAULT_PAYMENT_OPTIONS).isEqualTo(PaymentOptions.forFeeRateWeight(0));
    }
}
