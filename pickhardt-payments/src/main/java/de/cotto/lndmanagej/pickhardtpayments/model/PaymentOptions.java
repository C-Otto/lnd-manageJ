package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Pubkey;

import java.util.Optional;

public record PaymentOptions(
        Optional<Integer> feeRateWeight,
        Optional<Long> feeRateLimit,
        Optional<Long> feeRateLimitExceptIncomingHops,
        boolean ignoreFeesForOwnChannels,
        Optional<Pubkey> peer,
        Optional<Pubkey> peerForFirstHop
) {
    public static final PaymentOptions DEFAULT_PAYMENT_OPTIONS = forFeeRateWeight(0);

    public static PaymentOptions forFeeRateWeight(int feeRateWeight) {
        return new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.empty(),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static PaymentOptions forFeeRateLimit(long feeRateLimit) {
        return new PaymentOptions(
                Optional.of(0),
                Optional.of(feeRateLimit),
                Optional.empty(),
                true,
                Optional.empty(),
                Optional.empty()
        );
    }

    public static PaymentOptions forTopUp(int feeRateWeight, long feeRateLimit, long peerFeeRate, Pubkey peer) {
        return new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.of(feeRateLimit),
                Optional.of(Math.max(0, feeRateLimit - peerFeeRate)),
                false,
                Optional.of(peer),
                Optional.empty()
        );
    }

    public static PaymentOptions forTopUp(
            int feeRateWeight,
            long feeRateLimit,
            long peerFeeRate,
            Pubkey peer,
            Pubkey peerForFirstHop
    ) {
        return new PaymentOptions(
                Optional.of(feeRateWeight),
                Optional.of(feeRateLimit),
                Optional.of(Math.max(0, feeRateLimit - peerFeeRate)),
                false,
                Optional.of(peer),
                Optional.of(peerForFirstHop)
        );
    }
}
