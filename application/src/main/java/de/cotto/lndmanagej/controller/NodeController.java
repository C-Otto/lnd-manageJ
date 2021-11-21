package de.cotto.lndmanagej.controller;

import com.codahale.metrics.MetricRegistry;
import de.cotto.lndmanagej.controller.dto.ChannelsForNodeDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
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

    public NodeController(NodeService nodeService, ChannelService channelService, Metrics metrics) {
        this.nodeService = nodeService;
        this.metrics = metrics;
        this.channelService = channelService;
    }

    @GetMapping("/alias")
    public String getAlias(Pubkey pubkey) {
        mark("getAlias");
        return nodeService.getAlias(pubkey);
    }

    @GetMapping("/details")
    public NodeDetailsDto getDetails(@PathVariable Pubkey pubkey) {
        mark("getDetails");
        Node node = nodeService.getNode(pubkey);
        return new NodeDetailsDto(
                pubkey,
                node.alias(),
                toSortedList(channelService.getOpenChannelsWith(pubkey)),
                toSortedList(channelService.getClosedChannelsWith(pubkey)),
                toSortedList(channelService.getWaitingCloseChannelsFor(pubkey)),
                toSortedList(channelService.getForceClosingChannelsFor(pubkey)),
                node.online()
        );
    }

    @GetMapping("/open-channels")
    public ChannelsForNodeDto getOpenChannelIdsForPubkey(@PathVariable Pubkey pubkey) {
        mark("getOpenChannelIdsForPubkey");
        List<ChannelId> channels = toSortedList(channelService.getOpenChannelsWith(pubkey));
        return new ChannelsForNodeDto(pubkey, channels);
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
