package de.cotto.lndmanagej.feerates;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeRateInformation;

import java.time.LocalDateTime;

public record FeeRates(
        LocalDateTime timestamp,
        ChannelId channelId,
        FeeRateInformation feeRates
) {
}
