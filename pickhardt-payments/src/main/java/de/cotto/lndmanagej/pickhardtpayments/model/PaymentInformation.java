package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FailureCode;

import java.util.Optional;

public record PaymentInformation(Coins inFlight, boolean settled, Optional<FailureCode> failureCode) {
    public static final PaymentInformation DEFAULT = new PaymentInformation(Coins.NONE, false, Optional.empty());

    public PaymentInformation withAdditionalInFlight(Coins amount) {
        return new PaymentInformation(inFlight.add(amount), settled, failureCode);
    }

    public PaymentInformation withIsSettled() {
        return new PaymentInformation(inFlight, true, failureCode);
    }

    public PaymentInformation withFailureCode(FailureCode failureCode) {
        return new PaymentInformation(inFlight, settled, Optional.of(failureCode));
    }
}
