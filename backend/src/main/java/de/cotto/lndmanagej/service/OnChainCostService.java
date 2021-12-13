package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForceClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Resolution;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.transactions.model.Transaction;
import de.cotto.lndmanagej.transactions.service.TransactionService;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

@Component
public class OnChainCostService {
    private static final Coins ANCHOR = Coins.ofSatoshis(330);

    private final TransactionService transactionService;
    private final ChannelService channelService;

    public OnChainCostService(TransactionService transactionService, ChannelService channelService) {
        this.transactionService = transactionService;
        this.channelService = channelService;
    }

    @Timed
    public OnChainCosts getOnChainCostsForChannelId(ChannelId channelId) {
        return channelService.getLocalChannel(channelId)
                .map(this::getOnChainCostsForChannel)
                .orElse(OnChainCosts.NONE);
    }

    @Timed
    public OnChainCosts getOnChainCostsForChannel(LocalChannel localChannel) {
        return new OnChainCosts(
                getOpenCostsForChannel(localChannel).orElse(Coins.NONE),
                getCloseCostsForChannelId(localChannel.getId()).orElse(Coins.NONE),
                getSweepCostsForChannelId(localChannel.getId()).orElse(Coins.NONE)
        );
    }

    @Timed
    public OnChainCosts getOnChainCostsForPeer(Pubkey pubkey) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(this::getOnChainCostsForChannel)
                .reduce(OnChainCosts.NONE, OnChainCosts::add);
    }

    @Timed
    public Optional<Coins> getOpenCostsForChannelId(ChannelId channelId) {
        return channelService.getLocalChannel(channelId).flatMap(this::getOpenCostsForChannel);
    }

    @Timed
    public Optional<Coins> getOpenCostsForChannel(LocalChannel localChannel) {
        if (localChannel.getOpenInitiator().equals(OpenInitiator.LOCAL)) {
            TransactionHash openTransactionHash = localChannel.getChannelPoint().getTransactionHash();
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

    @Timed
    public Optional<Coins> getCloseCostsForChannelId(ChannelId channelId) {
        if (channelService.isClosed(channelId)) {
            return channelService.getClosedChannel(channelId).flatMap(this::getCloseCostsForChannel);
        }
        return Optional.of(Coins.NONE);
    }

    @Timed
    public Optional<Coins> getCloseCostsForChannel(ClosedChannel closedChannel) {
        if (closedChannel.getOpenInitiator().equals(OpenInitiator.LOCAL)) {
            return transactionService.getTransaction(closedChannel.getCloseTransactionHash())
                    .map(Transaction::fees);
        }
        if (closedChannel.getOpenInitiator().equals(OpenInitiator.REMOTE)) {
            return Optional.of(Coins.NONE);
        }
        return Optional.empty();
    }

    @Timed
    public Optional<Coins> getSweepCostsForChannelId(ChannelId channelId) {
        if (channelService.isForceClosed(channelId)) {
            return channelService.getForceClosedChannel(channelId).map(this::getSweepCostsForChannel);
        }
        return Optional.of(Coins.NONE);
    }

    @Timed
    public Coins getSweepCostsForChannel(ForceClosedChannel forceClosedChannel) {
        Set<Resolution> resolutions = forceClosedChannel.getResolutions();
        Coins anchorCosts = getAnchorCosts(forceClosedChannel, resolutions);
        return resolutions.stream()
                .filter(Resolution::sweepTransactionClaimsFunds)
                .map(Resolution::sweepTransaction)
                .flatMap(Optional::stream)
                .distinct()
                .map(transactionService::getTransaction)
                .flatMap(Optional::stream)
                .map(Transaction::fees)
                .reduce(anchorCosts, Coins::add);
    }

    private Coins getAnchorCosts(ForceClosedChannel forceClosedChannel, Set<Resolution> resolutions) {
        if (resolutions.stream().noneMatch(Resolution::isClaimedAnchor)) {
            return Coins.NONE;
        }
        boolean initiatedByPeer = forceClosedChannel.getOpenInitiator().equals(OpenInitiator.REMOTE);
        if (initiatedByPeer) {
            // peer pays for our anchor
            return Coins.NONE.subtract(ANCHOR);
        }
        // we pay for peer's anchor
        return ANCHOR;
    }

    private long getNumberOfChannelsWithOpenTransactionHash(TransactionHash openTransactionHash) {
        return channelService.getAllLocalChannels()
                .map(LocalChannel::getChannelPoint)
                .map(ChannelPoint::getTransactionHash)
                .filter(hash -> hash.equals(openTransactionHash))
                .count();
    }
}
