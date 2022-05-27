package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;

import javax.annotation.Nullable;
import java.util.Optional;

import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;

public class PaymentOptionsDto {
    public static final PaymentOptionsDto DEFAULT = new PaymentOptionsDto();

    static {
        DEFAULT.setFeeRateWeight(DEFAULT_PAYMENT_OPTIONS.feeRateWeight().orElse(null));
    }

    @Nullable
    private Integer feeRateWeight;
    @Nullable
    private Long feeRateLimit;
    private boolean ignoreFeesForOwnChannels;
    @Nullable
    private Pubkey peer;
    @Nullable
    private Long feeRateLimitExceptIncomingHops;

    public PaymentOptionsDto() {
        ignoreFeesForOwnChannels = DEFAULT_PAYMENT_OPTIONS.ignoreFeesForOwnChannels();
    }

    public PaymentOptions toModel() {

        return new PaymentOptions(
                Optional.ofNullable(feeRateWeight),
                Optional.ofNullable(feeRateLimit),
                Optional.ofNullable(feeRateLimitExceptIncomingHops),
                ignoreFeesForOwnChannels,
                Optional.ofNullable(peer)
        );
    }

    public void setFeeRateWeight(@Nullable Integer feeRateWeight) {
        this.feeRateWeight = feeRateWeight;
    }

    public void setFeeRateLimit(@Nullable Long feeRateLimit) {
        this.feeRateLimit = feeRateLimit;
    }

    public void setIgnoreFeesForOwnChannels(boolean ignoreFeesForOwnChannels) {
        this.ignoreFeesForOwnChannels = ignoreFeesForOwnChannels;
    }

    public void setPeer(@Nullable Pubkey peer) {
        this.peer = peer;
    }

    public void setFeeRateLimitExceptIncomingHops(@Nullable Long feeRateLimitExceptIncomingHops) {
        this.feeRateLimitExceptIncomingHops = feeRateLimitExceptIncomingHops;
    }
}
