package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.statistics.Balances;
import de.cotto.lndmanagej.statistics.StatisticsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;

@Component
@Transactional
class StatisticsDaoImpl implements StatisticsDao {
    private final StatisticsRepository statisticsRepository;

    public StatisticsDaoImpl(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void saveBalances(Balances balances) {
        statisticsRepository.save(BalancesJpaDto.fromModel(balances));
    }

    @Override
    public Optional<Balances> getMostRecentBalances(ChannelId channelId) {
        return statisticsRepository.findTopByChannelIdOrderByTimestampDesc(channelId.getShortChannelId())
                .map(BalancesJpaDto::toModel);
    }
}
