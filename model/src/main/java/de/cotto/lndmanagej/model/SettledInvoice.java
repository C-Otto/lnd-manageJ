package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.util.Optional;

public record SettledInvoice(
        long addIndex,
        long settleIndex,
        LocalDateTime settleDate,
        String hash,
        Coins amountPaid,
        String memo,
        Optional<String> keysendMessage,
        Optional<ChannelId> receivedVia
) {
    public static final SettledInvoice INVALID = new SettledInvoice(
            -1,
            -1,
            LocalDateTime.MIN,
            "",
            Coins.NONE,
            "",
            Optional.empty(),
            Optional.empty()
    );

    public boolean isValid() {
        return !INVALID.equals(this);
    }
}