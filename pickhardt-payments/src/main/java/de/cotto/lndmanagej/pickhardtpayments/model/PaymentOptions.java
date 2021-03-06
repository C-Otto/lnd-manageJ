package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.Optional;

public record PaymentOptions(
        Optional<Integer> feeRateWeight,
        Optional<Long> feeRateLimit,
        Optional<Long> feeRateLimitExceptIncomingHops,
        boolean ignoreFeesForOwnChannels,
        Optional<Pubkey> peer
) {
    public static final PaymentOptions DEFAULT_PAYMENT_OPTIONS = forFeeRateWeight(0);

    public static PaymentOptions forFeeRateWeight(int feeRateWeight) {
        return new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.empty(),
                Optional.empty(),
                true,
                Optional.empty()
        );
    }

    public static PaymentOptions forFeeRateLimit(long feeRateLimit) {
        return new PaymentOptions(
                Optional.of(0),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty()
        );
    }

    public static PaymentOptions forTopUp(long ourFeeRate, long peerFeeRate, Pubkey peer) {
        return new PaymentOptions(
                Optional.of(5),
                Optional.of(ourFeeRate),
                Optional.of(ourFeeRate - peerFeeRate),
                false,
                Optional.of(peer)
        );
    }
}
