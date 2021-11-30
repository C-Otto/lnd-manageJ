package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
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

    public OnChainCostsController(OnChainCostService onChainCostService) {
        this.onChainCostService = onChainCostService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/on-chain-costs")
    public OnChainCostsDto getCostsForPeer(@PathVariable Pubkey pubkey) {
        return new OnChainCostsDto(
                onChainCostService.getOpenCostsWith(pubkey),
                onChainCostService.getCloseCostsWith(pubkey)
        );
    }

    @Timed
    @GetMapping("/channel/{channelId}/open-costs")
    public long getOpenCostsForChannel(@PathVariable ChannelId channelId) throws CostException {
        return onChainCostService.getOpenCostsForChannelId(channelId).map(Coins::satoshis)
                .orElseThrow(() -> new CostException("Unable to get open costs for channel with ID " + channelId));
    }

    @Timed
    @GetMapping("/channel/{channelId}/close-costs")
    public long getCloseCostsForChannel(@PathVariable ChannelId channelId) throws CostException {
        return onChainCostService.getCloseCostsForChannelId(channelId).map(Coins::satoshis)
                .orElseThrow(() -> new CostException("Unable to get close costs for channel with ID " + channelId));
    }

}
