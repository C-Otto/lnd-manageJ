package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

public record SettledInvoice(
        long addIndex,
        long settleIndex,
        ZonedDateTime settleDate,
        String hash,
        Coins amountPaid,
        String memo,
        Optional<String> keysendMessage
) {
    public static final SettledInvoice INVALID = new SettledInvoice(
            -1,
            -1,
            LocalDateTime.MIN.atZone(ZoneOffset.UTC),
            "",
            Coins.NONE,
            "",
            Optional.empty()
    );

    public boolean isValid() {
        return !INVALID.equals(this);
    }
}
