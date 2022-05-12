package de.cotto.lndmanagej.ui.page.general;

import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;

import java.util.Comparator;
import java.util.List;

public class DashboardPage extends ThymeleafPage {

    public DashboardPage(List<OpenChannelDto> channels, List<NodeDto> nodes, NodesAndChannelsWithWarningsDto warnings) {
        super();
        List<OpenChannelDto> sortedChannels = channels.stream()
                .sorted(Comparator.comparing(OpenChannelDto::getOutboundPercentage))
                .toList();
        add("warnings", warnings);
        add("channels", sortedChannels);
        add("nodes", nodes);
    }

    @Override
    public String getView() {
        return "dashboard";
    }
}
