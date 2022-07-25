package de.cotto.lndmanagej.balances;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;

import java.util.Optional;

public interface BalancesDao {
    void saveBalances(Balances balances);

    Optional<Balances> getMostRecentBalances(ChannelId channelId);

    Optional<Coins> getLocalBalanceMinimum(ChannelId channelId, int days);

    Optional<Coins> getLocalBalanceMaximum(ChannelId channelId, int days);

    Optional<Coins> getLocalBalanceAverageOpenChannel(ChannelId channelId, int days);

    Optional<Coins> getLocalBalanceAverageClosedChannel(ChannelId channelId, int days);
}
