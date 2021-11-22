package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
public class OnChainCostsController {
    private final OnChainCostService onChainCostService;
    private final Metrics metrics;

    public OnChainCostsController(OnChainCostService onChainCostService, Metrics metrics) {
        this.onChainCostService = onChainCostService;
        this.metrics = metrics;
    }

    @GetMapping("/node/{pubkey}/on-chain-costs")
    public OnChainCostsDto getCostsForPeer(@PathVariable Pubkey pubkey) {
        metrics.mark(MetricRegistry.name(getClass(), "getCostsForPeer"));
        return new OnChainCostsDto(
                onChainCostService.getOpenCostsWith(pubkey),
                onChainCostService.getCloseCostsWith(pubkey)
        );
    }

    @GetMapping("/channel/{channelId}/open-costs")
    public long getOpenCostsForChannel(@PathVariable ChannelId channelId) throws CostException {
        metrics.mark(MetricRegistry.name(getClass(), "getOpenCostsForChannel"));
        return onChainCostService.getOpenCosts(channelId).map(Coins::satoshis)
                .orElseThrow(() -> new CostException("Unable to get open costs for channel with ID " + channelId));
    }

    @GetMapping("/channel/{channelId}/close-costs")
    public long getCloseCostsForChannel(@PathVariable ChannelId channelId) throws CostException {
        metrics.mark(MetricRegistry.name(getClass(), "getCloseCostsForChannel"));
        return onChainCostService.getCloseCosts(channelId).map(Coins::satoshis)
                .orElseThrow(() -> new CostException("Unable to get close costs for channel with ID " + channelId));
    }

}
