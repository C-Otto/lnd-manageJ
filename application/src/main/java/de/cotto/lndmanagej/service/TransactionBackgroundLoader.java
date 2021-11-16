package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.ClosedOrClosingChannel;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;

@Component
public class TransactionBackgroundLoader {
    private final ChannelService channelService;
    private final TransactionService transactionService;

    public TransactionBackgroundLoader(ChannelService channelService, TransactionService transactionService) {
        this.channelService = channelService;
        this.transactionService = transactionService;
    }

    @Scheduled(fixedDelay = 5, timeUnit = MINUTES)
    public void loadTransactionForOneChannel() {
        getTransactionHashes()
                .filter(transactionService::isUnknown)
                .findAny()
                .ifPresent(transactionService::getTransaction);
    }

    private Stream<String> getTransactionHashes() {
        Stream<String> openTransactionHashes = getOpenTransactionHashes();
        Stream<String> closeTransactionHashes = getCloseTransactionHashes();
        Stream<String> htlcOutpointHashes = getHtlcOutpointHashes();
        return Stream.of(openTransactionHashes, closeTransactionHashes, htlcOutpointHashes)
                .flatMap(s -> s);
    }

    private Stream<String> getOpenTransactionHashes() {
        return Stream.of(
                        channelService.getOpenChannels(),
                        channelService.getClosedChannels(),
                        channelService.getForceClosingChannels(),
                        channelService.getWaitingCloseChannels()
                )
                .flatMap(Collection::stream)
                .map(Channel::getChannelPoint)
                .map(ChannelPoint::getTransactionHash);
    }

    private Stream<String> getCloseTransactionHashes() {
        return Stream.of(channelService.getClosedChannels(), channelService.getForceClosingChannels())
                .flatMap(Collection::stream)
                .map(ClosedOrClosingChannel::getCloseTransactionHash);
    }

    private Stream<String> getHtlcOutpointHashes() {
        return channelService.getForceClosingChannels().stream()
                .map(ForceClosingChannel::getHtlcOutpoints)
                .flatMap(Collection::stream)
                .map(ChannelPoint::getTransactionHash);
    }

}
