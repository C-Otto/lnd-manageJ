package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;

public record Payment(
        long index,
        String paymentHash,
        LocalDateTime creationDateTime,
        Coins value,
        Coins fees
) {
}
