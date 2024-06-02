package de.cotto.lndmanagej.feerates;

import de.cotto.lndmanagej.model.ChannelId;

import java.util.Optional;

public interface FeeRatesDao {
    void saveFeeRates(FeeRates feeRates);

    Optional<FeeRates> getMostRecentFeeRates(ChannelId channelId);
}
