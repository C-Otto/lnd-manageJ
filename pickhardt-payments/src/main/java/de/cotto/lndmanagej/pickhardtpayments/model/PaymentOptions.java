package de.cotto.lndmanagej.pickhardtpayments.model;

public record PaymentOptions(int feeRateWeight) {
    public static final PaymentOptions DEFAULT_PAYMENT_OPTIONS = feeRateWeight(0);

    public static PaymentOptions feeRateWeight(int feeRateWeight) {
        return new PaymentOptions(feeRateWeight);
    }
}
