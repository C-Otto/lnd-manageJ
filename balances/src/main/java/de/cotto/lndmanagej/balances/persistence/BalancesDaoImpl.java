package de.cotto.lndmanagej.balances.persistence;

import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.balances.BalancesDao;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

@Component
@Transactional
class BalancesDaoImpl implements BalancesDao {
    private final BalancesRepository balancesRepository;

    public BalancesDaoImpl(BalancesRepository balancesRepository) {
        this.balancesRepository = balancesRepository;
    }

    @Override
    public void saveBalances(Balances balances) {
        balancesRepository.save(BalancesJpaDto.fromModel(balances));
    }

    @Override
    public Optional<Balances> getMostRecentBalances(ChannelId channelId) {
        return balancesRepository.findTopByChannelIdOrderByTimestampDesc(channelId.getShortChannelId())
                .map(BalancesJpaDto::toModel);
    }

    @Override
    public Optional<Coins> getLocalBalanceMinimum(ChannelId channelId, int days) {
        return getLocalBalance(
                balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(
                        channelId.getShortChannelId(),
                        getTimestamp(days)
                )
        );
    }

    @Override
    public Optional<Coins> getLocalBalanceMaximum(ChannelId channelId, int days) {
        long timestamp = getTimestamp(days);
        return getLocalBalance(
                balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(
                        channelId.getShortChannelId(),
                        timestamp
                )
        );
    }

    @Override
    public Optional<Coins> getLocalBalanceAverage(ChannelId channelId, int days) {
        long timestamp = getTimestamp(days);
        return balancesRepository.getAverageLocalBalance(
                channelId.getShortChannelId(),
                timestamp
        ).map(Coins::ofSatoshis);
    }

    private Optional<Coins> getLocalBalance(Optional<BalancesJpaDto> balances) {
        return balances
                .map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance);
    }

    private long getTimestamp(int daysInPast) {
        return ZonedDateTime.now(ZoneOffset.UTC).minusDays(daysInPast).toEpochSecond();
    }
}
