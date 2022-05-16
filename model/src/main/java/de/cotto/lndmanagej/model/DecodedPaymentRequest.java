package de.cotto.lndmanagej.model;

import java.time.Instant;
import java.util.Set;

public record DecodedPaymentRequest(
        String paymentRequest,
        int cltvExpiry,
        String description,
        Pubkey destination,
        Coins amount,
        HexString paymentHash,
        HexString paymentAddress,
        Instant creation,
        Instant expiry,
        Set<RouteHint> routeHints
) {
}
