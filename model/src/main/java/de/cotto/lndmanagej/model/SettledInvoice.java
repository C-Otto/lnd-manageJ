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
        ChannelId receivedVia
) {
    private static final ChannelId SOME_CHANNEL_ID = ChannelId.fromShortChannelId(430_103_660_018_532_352L);
    public static final SettledInvoice INVALID = new SettledInvoice(
            -1,
            -1,
            LocalDateTime.MIN,
            "",
            Coins.NONE,
            "",
            Optional.empty(),
            SOME_CHANNEL_ID
    );

    public boolean isValid() {
        return !INVALID.equals(this);
    }
}