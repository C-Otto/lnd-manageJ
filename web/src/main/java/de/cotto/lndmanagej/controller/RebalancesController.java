package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.OffChainCostService;
import de.cotto.lndmanagej.service.RebalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class RebalancesController {
    private final OffChainCostService offChainCostService;
    private final RebalanceService rebalanceService;

    public RebalancesController(OffChainCostService offChainCostService, RebalanceService rebalanceService) {
        this.offChainCostService = offChainCostService;
        this.rebalanceService = rebalanceService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-source-costs")
    public long getRebalanceSourceCostsForPeer(@PathVariable Pubkey pubkey) {
        return offChainCostService.getRebalanceSourceCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-source-amount")
    public long getRebalanceSourceAmountForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getRebalanceAmountFromPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-source-costs")
    public long getRebalanceSourceCostsForChannel(@PathVariable ChannelId channelId) {
        return offChainCostService.getRebalanceSourceCostsForChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-source-amount")
    public long getRebalanceSourceAmountForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getRebalanceAmountFromChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-target-costs")
    public long getRebalanceTargetCostsForPeer(@PathVariable Pubkey pubkey) {
        return offChainCostService.getRebalanceTargetCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-target-amount")
    public long getRebalanceTargetAmountForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getRebalanceAmountToPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-target-costs")
    public long getRebalanceTargetCostsForChannel(@PathVariable ChannelId channelId) {
        return offChainCostService.getRebalanceTargetCostsForChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-target-amount")
    public long getRebalanceTargetAmountForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getRebalanceAmountToChannel(channelId).milliSatoshis();
    }
}
