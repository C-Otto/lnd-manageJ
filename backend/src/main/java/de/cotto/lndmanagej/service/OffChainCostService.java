package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.OffChainCosts;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.SelfPayment;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class OffChainCostService {
    private final RebalanceService rebalanceService;

    public OffChainCostService(RebalanceService rebalanceService) {
        this.rebalanceService = rebalanceService;
    }

    @Timed
    public OffChainCosts getOffChainCostsForPeer(Pubkey pubkey) {
        return new OffChainCosts(
                getRebalanceSourceCostsForPeer(pubkey),
                getRebalanceTargetCostsForPeer(pubkey)
        );
    }

    @Timed
    public OffChainCosts getOffChainCostsForChannel(ChannelId channelId) {
        return new OffChainCosts(
                getRebalanceSourceCostsForChannel(channelId),
                getRebalanceTargetCostsForChannel(channelId)
        );
    }

    @Timed
    public Coins getRebalanceSourceCostsForChannel(ChannelId channelId) {
        return getSumOfFees(rebalanceService.getRebalancesFromChannel(channelId));
    }

    @Timed
    public Coins getRebalanceSourceCostsForPeer(Pubkey pubkey) {
        return getSumOfFees(rebalanceService.getRebalancesFromPeer(pubkey));
    }

    @Timed
    public Coins getRebalanceTargetCostsForChannel(ChannelId channelId) {
        return getSumOfFees(rebalanceService.getRebalancesToChannel(channelId));
    }

    @Timed
    public Coins getRebalanceTargetCostsForPeer(Pubkey pubkey) {
        return getSumOfFees(rebalanceService.getRebalancesToPeer(pubkey));
    }

    private Coins getSumOfFees(Set<SelfPayment> selfPayments) {
        return selfPayments.stream()
                .map(SelfPayment::fees)
                .reduce(Coins.NONE, Coins::add);
    }
}
