package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ClosedOrClosingChannel;
import de.cotto.lndmanagej.model.ForceClosedChannel;
import de.cotto.lndmanagej.model.Resolution;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
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

    private Stream<TransactionHash> getTransactionHashes() {
        Stream<TransactionHash> openTransactionHashes = getOpenTransactionHashes();
        Stream<TransactionHash> closeTransactionHashes = getCloseTransactionHashes();
        Stream<TransactionHash> sweepTransactionHashes = getSweepTransactionHashes();
        return Stream.of(openTransactionHashes, closeTransactionHashes, sweepTransactionHashes)
                .flatMap(s -> s);
    }

    private Stream<TransactionHash> getOpenTransactionHashes() {
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

    private Stream<TransactionHash> getCloseTransactionHashes() {
        return Stream.of(channelService.getClosedChannels(), channelService.getForceClosingChannels())
                .flatMap(Collection::stream)
                .map(ClosedOrClosingChannel::getCloseTransactionHash);
    }

    private Stream<TransactionHash> getSweepTransactionHashes() {
        return channelService.getClosedChannels().stream()
                .filter(ClosedChannel::isForceClosed)
                .map(ClosedChannel::getAsForceClosedChannel)
                .map(ForceClosedChannel::getResolutions)
                .flatMap(Collection::stream)
                .filter(Resolution::sweepTransactionClaimsFunds)
                .map(Resolution::sweepTransaction)
                .flatMap(Optional::stream);
    }
}
