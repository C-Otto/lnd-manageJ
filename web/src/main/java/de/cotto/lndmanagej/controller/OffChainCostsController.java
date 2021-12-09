package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.OffChainCostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class OffChainCostsController {
    private final OffChainCostService offChainCostService;

    public OffChainCostsController(OffChainCostService offChainCostService) {
        this.offChainCostService = offChainCostService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-source-costs")
    public long getRebalanceSourceCostsForPeer(@PathVariable Pubkey pubkey) {
        return offChainCostService.getRebalanceSourceCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-source-costs")
    public long getRebalanceSourceCostsForChannel(@PathVariable ChannelId channelId) {
        return offChainCostService.getRebalanceSourceCostsForChannel(channelId).milliSatoshis();
    }

    @Timed
    @GetMapping("/node/{pubkey}/rebalance-target-costs")
    public long getRebalanceTargetCostsForPeer(@PathVariable Pubkey pubkey) {
        return offChainCostService.getRebalanceTargetCostsForPeer(pubkey).milliSatoshis();
    }

    @Timed
    @GetMapping("/channel/{channelId}/rebalance-target-costs")
    public long getRebalanceTargetCostsForChannel(@PathVariable ChannelId channelId) {
        return offChainCostService.getRebalanceTargetCostsForChannel(channelId).milliSatoshis();
    }
}
