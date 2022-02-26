package de.cotto.lndmanagej.controller;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.ObjectMapperConfiguration;
import de.cotto.lndmanagej.controller.dto.WarningsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Map.Entry.comparingByKey;

@RestController
@RequestMapping("/api/")
@Import(ObjectMapperConfiguration.class)
public class WarningsController {
    private final NodeWarningsService nodeWarningsService;
    private final ChannelWarningsService channelWarningsService;

    public WarningsController(
            NodeWarningsService nodeWarningsService,
            ChannelWarningsService channelWarningsService
    ) {
        this.nodeWarningsService = nodeWarningsService;
        this.channelWarningsService = channelWarningsService;
    }

    @Timed
    @GetMapping("/node/{pubkey}/warnings")
    public WarningsDto getWarningsForNode(@PathVariable Pubkey pubkey) {
        return WarningsDto.createFromModel(nodeWarningsService.getNodeWarnings(pubkey));
    }

    @Timed
    @GetMapping("/channel/{channelId}/warnings")
    public WarningsDto getWarningsForChannel(@PathVariable ChannelId channelId) {
        return WarningsDto.createFromModel(channelWarningsService.getChannelWarnings(channelId));
    }

    @Timed
    @GetMapping("/warnings")
    public NodesAndChannelsWithWarningsDto getWarnings() {
        Map<Node, NodeWarnings> nodeWarnings = nodeWarningsService.getNodeWarnings();
        List<NodeWithWarningsDto> nodeWarningsList = nodeWarnings.entrySet().stream()
                .sorted(comparingByKey())
                .map(entry -> getNodeWarningsDto(entry.getKey(), entry.getValue()))
                .toList();
        Map<LocalOpenChannel, ChannelWarnings> channelWarnings = channelWarningsService.getChannelWarnings();
        List<ChannelWithWarningsDto> channelWarningsList = channelWarnings.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().getId()))
                .map(entry -> getChannelWarningsDto(entry.getKey(), entry.getValue()))
                .toList();
        return new NodesAndChannelsWithWarningsDto(nodeWarningsList, channelWarningsList);
    }

    private NodeWithWarningsDto getNodeWarningsDto(Node node, NodeWarnings warnings) {
        return new NodeWithWarningsDto(
                WarningsDto.createFromModel(warnings).warnings(),
                node.alias(),
                node.pubkey()
        );
    }

    private ChannelWithWarningsDto getChannelWarningsDto(LocalOpenChannel channel, ChannelWarnings warnings) {
        return new ChannelWithWarningsDto(
                WarningsDto.createFromModel(warnings).warnings(),
                channel.getId()
        );
    }
}
