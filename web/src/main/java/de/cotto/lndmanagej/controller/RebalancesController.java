package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.RebalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class RebalancesController {
    private final RebalanceService rebalanceService;

    public RebalancesController(RebalanceService rebalanceService) {
        this.rebalanceService = rebalanceService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-source-costs")
    public long getRebalanceSourceCostsForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getSourceCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-source-amount")
    public long getRebalanceSourceAmountForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getAmountFromPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-source-costs")
    public long getRebalanceSourceCostsForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getSourceCostsForChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-source-amount")
    public long getRebalanceSourceAmountForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getAmountFromChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-target-costs")
    public long getRebalanceTargetCostsForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getTargetCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-target-amount")
    public long getRebalanceTargetAmountForPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getAmountToPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-target-costs")
    public long getRebalanceTargetCostsForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getTargetCostsForChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-target-amount")
    public long getRebalanceTargetAmountForChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getAmountToChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-support-as-source-amount")
    public long getRebalanceSupportAsSourceAmountFromChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getSupportAsSourceAmountFromChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-support-as-source-amount")
    public long getRebalanceSupportAsSourceAmountFromPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getSupportAsSourceAmountFromPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-support-as-target-amount")
    public long getRebalanceSupportAsTargetAmountToChannel(@PathVariable ChannelId channelId) {
        return rebalanceService.getSupportAsTargetAmountToChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-support-as-target-amount")
    public long getRebalanceSupportAsTargetAmountToPeer(@PathVariable Pubkey pubkey) {
        return rebalanceService.getSupportAsTargetAmountToPeer(pubkey).milliSatoshis();
    }
}
