package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.NodeDetails;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeDetailsService;
import de.cotto.lndmanagej.service.NodeService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Period;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/node/{pubkey}")
@Import(ObjectMapperConfiguration.class)
public class NodeController {
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final FeeService feeService;
    private final NodeDetailsService nodeDetailsService;

    public NodeController(
            NodeService nodeService,
            ChannelService channelService,
            BalanceService balanceService,
            FeeService feeService,
            NodeDetailsService nodeDetailsService
    ) {
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.feeService = feeService;
        this.nodeDetailsService = nodeDetailsService;
    }

    @Timed
    @GetMapping("/alias")
    public String getAlias(@PathVariable Pubkey pubkey) {
        return nodeService.getAlias(pubkey);
    }

    @Timed
    @GetMapping("/details")
    public NodeDetailsDto getDetails(@PathVariable Pubkey pubkey) {
        NodeDetails nodeDetails = nodeDetailsService.getDetails(pubkey);
        return NodeDetailsDto.createFromModel(nodeDetails);
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
        return FeeReportDto.createFromModel(feeService.getFeeReportForPeer(pubkey));
    }

    @Timed
    @GetMapping("/fee-report/last-days/{lastDays}")
    public FeeReportDto getFeeReport(@PathVariable Pubkey pubkey, @PathVariable int lastDays) {
        return FeeReportDto.createFromModel(feeService.getFeeReportForPeer(pubkey, Duration.ofDays(lastDays)));
    }

    private List<ChannelId> toSortedList(Set<? extends Channel> channels) {
        return channels.stream()
                .map(Channel::getId)
                .sorted()
                .toList();
    }
}
