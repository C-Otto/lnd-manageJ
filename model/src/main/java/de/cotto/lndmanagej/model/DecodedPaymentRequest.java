package de.cotto.lndmanagej.model;

import java.time.Instant;

public record DecodedPaymentRequest(
        String paymentRequest,
        long cltvExpiry,
        String description,
        Pubkey destination,
        Coins amount,
        HexString paymentHash,
        HexString paymentAddress,
        Instant creation,
        Instant expiry
) {
}
