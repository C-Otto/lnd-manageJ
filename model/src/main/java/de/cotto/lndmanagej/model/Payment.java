package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.util.List;

public record Payment(
        long index,
        String paymentHash,
        LocalDateTime creationDateTime,
        Coins value,
        Coins fees,
        List<PaymentRoute> routes
) {
}
