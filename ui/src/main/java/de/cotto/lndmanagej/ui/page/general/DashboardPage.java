package de.cotto.lndmanagej.ui.page.general;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.List;

public class DashboardPage extends ThymeleafPage {

    private final List<NodeDto> nodes;

    public DashboardPage(List<OpenChannelDto> channels, List<NodeDto> nodes, NodesAndChannelsWithWarningsDto warnings) {
        super();
        this.nodes = sort(nodes);
        add("warnings", warnings);
        add("channels", channels);
        add("nodes", this.nodes);
    }

    private List<NodeDto> sort(List<NodeDto> nodes) {
        return nodes.stream()
                .sorted(NodeDto.getDefaultComparator())
                .toList();
    }

    public List<NodeDto> getNodes() {
        return nodes;
    }

    @Override
    public String getView() {
        return "dashboard";
    }

}
