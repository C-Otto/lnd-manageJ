package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.service.ChannelService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class BalanceStatisticsService {
    private final ChannelService channelService;
    private final BalancesDao balancesDao;

    public BalanceStatisticsService(ChannelService channelService, BalancesDao balancesDao) {
        this.channelService = channelService;
        this.balancesDao = balancesDao;
    }

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void storeBalances() {
        LocalDateTime timestamp = LocalDateTime.now(ZoneOffset.UTC);
        channelService.getOpenChannels().forEach(channel -> storeUpdatedBalance(channel, timestamp));
    }

    private void storeUpdatedBalance(LocalOpenChannel channel, LocalDateTime timestamp) {
        BalanceInformation balanceInformation = channel.getBalanceInformation();
        Optional<Balances> persistedBalances = balancesDao.getMostRecentBalances(channel.getId());
        if (persistedBalances.isPresent() && balanceInformation.equals(persistedBalances.get().balanceInformation())) {
            return;
        }
        balancesDao.saveBalances(new Balances(timestamp, channel.getId(), balanceInformation));
    }
}
