package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.OffChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OffChainCostService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/node/{pubkey}")
@Import(ObjectMapperConfiguration.class)
public class NodeController {
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final OnChainCostService onChainCostService;
    private final OffChainCostService offChainCostService;
    private final BalanceService balanceService;
    private final FeeService feeService;

    public NodeController(
            NodeService nodeService,
            ChannelService channelService,
            OnChainCostService onChainCostService,
            OffChainCostService offChainCostService,
            BalanceService balanceService,
            FeeService feeService
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.onChainCostService = onChainCostService;
        this.offChainCostService = offChainCostService;
        this.balanceService = balanceService;
        this.feeService = feeService;
    }

    @Timed
    @GetMapping("/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        return nodeService.getAlias(pubkey);
    }

    @Timed
    @GetMapping("/details")
    public NodeDetailsDto getDetails(@PathVariable Pubkey pubkey) {
        Node node = nodeService.getNode(pubkey);
        OnChainCosts onChainCosts = onChainCostService.getOnChainCostsForPeer(pubkey);
        Coins rebalanceSourceCosts = offChainCostService.getRebalanceSourceCostsForPeer(pubkey);
        Coins rebalanceTargetCosts = offChainCostService.getRebalanceTargetCostsForPeer(pubkey);
        BalanceInformation balanceInformation = balanceService.getBalanceInformationForPeer(pubkey);
        return new NodeDetailsDto(
                pubkey,
                node.alias(),
                toSortedList(channelService.getOpenChannelsWith(pubkey)),
                toSortedList(channelService.getClosedChannelsWith(pubkey)),
                toSortedList(channelService.getWaitingCloseChannelsWith(pubkey)),
                toSortedList(channelService.getForceClosingChannelsWith(pubkey)),
                OnChainCostsDto.createFromModel(onChainCosts),
                new OffChainCostsDto(rebalanceSourceCosts, rebalanceTargetCosts),
                BalanceInformationDto.createFromModel(balanceInformation),
                node.online(),
                getFeeReportDto(pubkey)
        );
    }

    @Timed
    @GetMapping("/open-channels")
    public ChannelsForNodeDto getOpenChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        List<ChannelId> channels = toSortedList(channelService.getOpenChannelsWith(pubkey));
        return new ChannelsForNodeDto(pubkey, channels);
    }

    @Timed
    @GetMapping("/all-channels")
    public ChannelsForNodeDto getAllChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        List<ChannelId> channels = toSortedList(channelService.getAllChannelsWith(pubkey));
        return new ChannelsForNodeDto(pubkey, channels);
    }

    @Timed
    @GetMapping("/balance")
    public BalanceInformationDto getBalance(@PathVariable Pubkey pubkey) {
        return BalanceInformationDto.createFromModel(balanceService.getBalanceInformationForPeer(pubkey));
    }

    @Timed
    @GetMapping("/fee-report")
    public FeeReportDto getFeeReport(@PathVariable Pubkey pubkey) {
        return getFeeReportDto(pubkey);
    }

    private FeeReportDto getFeeReportDto(Pubkey pubkey) {
        return FeeReportDto.createFromModel(feeService.getFeeReportForPeer(pubkey));
    }

    private List<ChannelId> toSortedList(Set<? extends Channel> channels) {
        return channels.stream()
                .map(Channel::getId)
                .sorted()
                .toList();
    }
}
