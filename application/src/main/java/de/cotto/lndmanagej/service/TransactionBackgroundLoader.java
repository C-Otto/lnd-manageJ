package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class TransactionBackgroundLoader {
    private final ChannelService channelService;
    private final TransactionService transactionService;

    public TransactionBackgroundLoader(ChannelService channelService, TransactionService transactionService) {
        this.channelService = channelService;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void loadTransactionForOneChannel() {
        channelService.getOpenChannels().stream()
                .map(Channel::getChannelPoint)
                .map(ChannelPoint::getTransactionHash)
                .filter(transactionService::isUnknown)
                .findAny()
                .ifPresent(transactionService::getTransaction);
    }
}
