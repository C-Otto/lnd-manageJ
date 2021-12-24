package de.cotto.lndmanagej.balances;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;

import java.time.LocalDateTime;

public record Balances(
        LocalDateTime timestamp,
        ChannelId channelId,
        BalanceInformation balanceInformation
) {
}
