package de.cotto.lndmanagej.model;

import java.util.Optional;

public record PaymentRoute(Optional<PaymentHop> firstHop, Optional<PaymentHop> lastHop) {
    public PaymentRoute(PaymentHop firstHop, PaymentHop lastHop) {
        this(Optional.of(firstHop), Optional.of(lastHop));
    }
}
