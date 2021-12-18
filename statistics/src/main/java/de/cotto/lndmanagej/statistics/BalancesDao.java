package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.ChannelId;

import java.util.Optional;

public interface BalancesDao {
    void saveBalances(Balances balances);

    Optional<Balances> getMostRecentBalances(ChannelId channelId);
}
