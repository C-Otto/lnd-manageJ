package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentOptionsDtoTest {
    @Test
    void toModel_default() {
        assertThat(new PaymentOptionsDto().toModel()).isEqualTo(DEFAULT_PAYMENT_OPTIONS);
    }

    @Test
    void just_fee_rate_weight() {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        dto.setFeeRateWeight(123);
        assertThat(dto.toModel()).isEqualTo(PaymentOptions.forFeeRateWeight(123));
    }

    @Test
    void feeRateLimit() {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        long feeRateLimit = 555;
        dto.setFeeRateLimit(feeRateLimit);
        PaymentOptions expected = new PaymentOptions(
                DEFAULT_PAYMENT_OPTIONS.feeRateWeight(),
                Optional.of(feeRateLimit),
                Optional.empty(),
                DEFAULT_PAYMENT_OPTIONS.ignoreFeesForOwnChannels(),
                Optional.empty()
        );
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void feeRateLimit_and_feeRateLimitExceptIncomingHops() {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        long feeRateLimit = 555;
        long feeRateLimitExceptIncomingHops = 777;
        dto.setFeeRateLimit(feeRateLimit);
        dto.setFeeRateLimitExceptIncomingHops(feeRateLimitExceptIncomingHops);
        PaymentOptions expected = new PaymentOptions(
                DEFAULT_PAYMENT_OPTIONS.feeRateWeight(),
                Optional.of(feeRateLimit),
                Optional.of(feeRateLimitExceptIncomingHops),
                DEFAULT_PAYMENT_OPTIONS.ignoreFeesForOwnChannels(),
                Optional.empty()
        );
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void ignoreFeesForOwnChannels() {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        dto.setIgnoreFeesForOwnChannels(false);
        PaymentOptions expected = new PaymentOptions(
                DEFAULT_PAYMENT_OPTIONS.feeRateWeight(),
                Optional.empty(),
                Optional.empty(),
                false,
                Optional.empty()
        );
        assertThat(dto.toModel()).isEqualTo(expected);
    }

    @Test
    void peer() {
        PaymentOptionsDto dto = new PaymentOptionsDto();
        dto.setPeer(PUBKEY);
        PaymentOptions expected = new PaymentOptions(
                DEFAULT_PAYMENT_OPTIONS.feeRateWeight(),
                Optional.empty(),
                Optional.empty(),
                DEFAULT_PAYMENT_OPTIONS.ignoreFeesForOwnChannels(),
                Optional.of(PUBKEY)
        );
        assertThat(dto.toModel()).isEqualTo(expected);
    }
}
