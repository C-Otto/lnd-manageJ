package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.statistics.Statistics;
import de.cotto.lndmanagej.statistics.StatisticsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
@Transactional
public class StatisticsDaoImpl implements StatisticsDao {
    private final StatisticsRepository statisticsRepository;

    public StatisticsDaoImpl(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    @Override
    public void saveStatistics(Statistics statistics) {
        statisticsRepository.save(StatisticsJpaDto.fromModel(statistics));
    }
}
