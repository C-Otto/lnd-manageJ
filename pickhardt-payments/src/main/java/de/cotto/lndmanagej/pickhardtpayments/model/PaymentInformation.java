package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

public record PaymentInformation(Coins inFlight, boolean settled, boolean failed) {
    public static final PaymentInformation DEFAULT = new PaymentInformation(Coins.NONE, false, false);

    public PaymentInformation withAdditionalInFlight(Coins amount) {
        return new PaymentInformation(inFlight.add(amount), settled, failed);
    }

    public PaymentInformation withIsSettled() {
        return new PaymentInformation(inFlight, true, failed);
    }

    public PaymentInformation withIsFailed() {
        return new PaymentInformation(inFlight, settled, true);
    }
}
