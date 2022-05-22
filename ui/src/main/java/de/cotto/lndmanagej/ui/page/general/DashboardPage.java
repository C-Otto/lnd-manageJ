package de.cotto.lndmanagej.ui.page.general;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.Comparator;
import java.util.List;

public class DashboardPage extends ThymeleafPage {

    private final List<OpenChannelDto> channels;
    private final List<NodeDto> nodes;

    public DashboardPage(List<OpenChannelDto> channels, List<NodeDto> nodes, NodesAndChannelsWithWarningsDto warnings) {
        super();
        this.channels = sortByChannelRatio(channels);
        this.nodes = sort(nodes);
        add("warnings", warnings);
        add("channels", this.channels);
        add("nodes", this.nodes);
    }

    private List<NodeDto> sort(List<NodeDto> nodes) {
        return nodes.stream().sorted(new NodeDto.OnlineStatusAndAliasComparator()).toList();
    }

    private List<OpenChannelDto> sortByChannelRatio(List<OpenChannelDto> channels) {
        return channels.stream()
                .sorted(Comparator.comparing(OpenChannelDto::getOutboundPercentage))
                .toList();
    }

    public List<OpenChannelDto> getChannels() {
        return channels;
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }

    @Override
    public String getView() {
        return "dashboard";
    }

}
