package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;

import java.time.LocalDateTime;

public record Statistics(
        LocalDateTime timestamp,
        ChannelId channelId,
        BalanceInformation balanceInformation
) {
}
