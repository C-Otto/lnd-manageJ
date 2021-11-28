package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.transactions.model.Transaction;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class OnChainCostService {
    private final TransactionService transactionService;
    private final ChannelService channelService;

    public OnChainCostService(TransactionService transactionService, ChannelService channelService) {
        this.transactionService = transactionService;
        this.channelService = channelService;
    }

    public Coins getOpenCostsWith(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(this::getOpenCosts)
                .flatMap(Optional::stream)
                .reduce(Coins.NONE, Coins::add);
    }

    public Optional<Coins> getOpenCosts(ChannelId channelId) {
        return channelService.getLocalChannel(channelId).flatMap(this::getOpenCosts);
    }

    public Optional<Coins> getOpenCosts(LocalChannel localChannel) {
        if (localChannel.getOpenInitiator().equals(OpenInitiator.LOCAL)) {
            String openTransactionHash = localChannel.getChannelPoint().getTransactionHash();
            return transactionService.getTransaction(openTransactionHash)
                    .map(Transaction::fees)
                    .map(Coins::satoshis)
                    .map(sat -> {
                        long channels = getNumberOfChannelsWithOpenTransactionHash(openTransactionHash);
                        return Coins.ofSatoshis(sat / channels);
                    });
        }
        if (localChannel.getOpenInitiator().equals(OpenInitiator.REMOTE)) {
            return Optional.of(Coins.NONE);
        }
        return Optional.empty();
    }

    public Coins getCloseCostsWith(Pubkey pubkey) {
        return channelService.getClosedChannelsWith(pubkey).parallelStream()
                .map(this::getCloseCosts)
                .flatMap(Optional::stream)
                .reduce(Coins.NONE, Coins::add);
    }

    public Optional<Coins> getCloseCosts(ChannelId channelId) {
        if (channelService.isClosed(channelId)) {
            return channelService.getClosedChannel(channelId).flatMap(this::getCloseCosts);
        }
        return Optional.of(Coins.NONE);
    }

    public Optional<Coins> getCloseCosts(ClosedChannel closedChannel) {
        if (closedChannel.getOpenInitiator().equals(OpenInitiator.LOCAL)) {
            return transactionService.getTransaction(closedChannel.getCloseTransactionHash())
                    .map(Transaction::fees);
        }
        if (closedChannel.getOpenInitiator().equals(OpenInitiator.REMOTE)) {
            return Optional.of(Coins.NONE);
        }
        return Optional.empty();
    }

    private long getNumberOfChannelsWithOpenTransactionHash(String openTransactionHash) {
        return channelService.getAllLocalChannels()
                .map(LocalChannel::getChannelPoint)
                .map(ChannelPoint::getTransactionHash)
                .filter(x -> x.equals(openTransactionHash))
                .count();
    }
}
