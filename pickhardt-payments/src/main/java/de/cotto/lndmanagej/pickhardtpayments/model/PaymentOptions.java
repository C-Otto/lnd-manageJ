package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.Optional;

public record PaymentOptions(
        int feeRateWeight,
        Optional<Long> feeRateLimit,
        boolean ignoreFeesForOwnChannels,
        Optional<Pubkey> peer
) {
    public static final PaymentOptions DEFAULT_PAYMENT_OPTIONS = forFeeRateWeight(0);

    public static PaymentOptions forFeeRateWeight(int feeRateWeight) {
        return new PaymentOptions(feeRateWeight, Optional.empty(), true, Optional.empty());
    }

    public static PaymentOptions forFeeRateLimit(long feeRateLimit) {
        return new PaymentOptions(0, Optional.of(feeRateLimit), true, Optional.empty());
    }

    public static PaymentOptions forTopUp(long feeRateLimit, Pubkey peer) {
        return new PaymentOptions(5, Optional.of(feeRateLimit), false, Optional.of(peer));
    }
}
