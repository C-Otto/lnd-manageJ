package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.service.ChannelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@Component
public class StatisticsService {
    private final ChannelService channelService;
    private final StatisticsDao statisticsDao;

    public StatisticsService(ChannelService channelService, StatisticsDao statisticsDao) {
        this.channelService = channelService;
        this.statisticsDao = statisticsDao;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void storeBalances() {
        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
        channelService.getOpenChannels().forEach(channel -> storeBalance(channel, timestamp));
    }

    private void storeBalance(LocalOpenChannel channel, LocalDateTime timestamp) {
        ChannelId channelId = channel.getId();
        BalanceInformation balanceInformation = channel.getBalanceInformation();
        Statistics statistics = new Statistics(timestamp, channelId, balanceInformation);
        statisticsDao.saveStatistics(statistics);
    }
}
