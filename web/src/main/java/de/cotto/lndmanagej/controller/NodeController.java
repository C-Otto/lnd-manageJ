package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/node/{pubkey}")
@Import(ObjectMapperConfiguration.class)
public class NodeController {
    private final NodeService nodeService;
    private final Metrics metrics;
    private final ChannelService channelService;
    private final OnChainCostService onChainCostService;
    private final BalanceService balanceService;
    private final FeeService feeService;

    public NodeController(
            NodeService nodeService,
            ChannelService channelService,
            OnChainCostService onChainCostService,
            BalanceService balanceService,
            FeeService feeService,
            Metrics metrics
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.onChainCostService = onChainCostService;
        this.balanceService = balanceService;
        this.feeService = feeService;
        this.metrics = metrics;
    }

    @GetMapping("/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        mark("getAlias");
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/details")
    public NodeDetailsDto getDetails(@PathVariable Pubkey pubkey) {
        mark("getDetails");
        Node node = nodeService.getNode(pubkey);
        Coins openCosts = onChainCostService.getOpenCostsWith(pubkey);
        Coins closeCosts = onChainCostService.getCloseCostsWith(pubkey);
        BalanceInformation balanceInformation = balanceService.getBalanceInformation(pubkey);
        return new NodeDetailsDto(
                pubkey,
                node.alias(),
                toSortedList(channelService.getOpenChannelsWith(pubkey)),
                toSortedList(channelService.getClosedChannelsWith(pubkey)),
                toSortedList(channelService.getWaitingCloseChannelsFor(pubkey)),
                toSortedList(channelService.getForceClosingChannelsFor(pubkey)),
                new OnChainCostsDto(openCosts, closeCosts),
                BalanceInformationDto.createFrom(balanceInformation),
                node.online(),
                getFeeReportDto(pubkey)
        );
    }

    @GetMapping("/open-channels")
    public ChannelsForNodeDto getOpenChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        mark("getOpenChannelIdsForPubkey");
        List<ChannelId> channels = toSortedList(channelService.getOpenChannelsWith(pubkey));
        return new ChannelsForNodeDto(pubkey, channels);
    }

    @GetMapping("/all-channels")
    public ChannelsForNodeDto getAllChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        mark("getAllChannelIdsForPubkey");
        List<ChannelId> channels = toSortedList(channelService.getAllChannelsWith(pubkey));
        return new ChannelsForNodeDto(pubkey, channels);
    }

    @GetMapping("/balance")
    public BalanceInformationDto getBalance(@PathVariable Pubkey pubkey) {
        mark("getBalance");
        return BalanceInformationDto.createFrom(balanceService.getBalanceInformation(pubkey));
    }

    @GetMapping("/fee-report")
    public FeeReportDto getFeeReport(@PathVariable Pubkey pubkey) {
        mark("getFeeReport");
        return getFeeReportDto(pubkey);
    }

    private FeeReportDto getFeeReportDto(Pubkey pubkey) {
        return FeeReportDto.createFrom(feeService.getFeeReportForPeer(pubkey));
    }

    private List<ChannelId> toSortedList(Set<? extends Channel> channels) {
        return channels.stream()
                .map(Channel::getId)
                .sorted()
                .collect(Collectors.toList());
    }

    private void mark(String getDetails) {
        metrics.mark(MetricRegistry.name(getClass(), getDetails));
    }

}
