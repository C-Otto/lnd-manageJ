package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PageService {

    private final UiDataService dataService;

    public PageService(UiDataService dataService) {
        this.dataService = dataService;
    }

    public DashboardPage dashboard() {
        return new DashboardPage(dataService.getOpenChannels(), dataService.createNodeList(), dataService.getStatus());
    }

    public ChannelsPage channels() {
        return new ChannelsPage(dataService.getOpenChannels());
    }

    public ChannelDetailsPage channelDetails(ChannelId channelId) throws NotFoundException {
        return new ChannelDetailsPage(dataService.getChannelDetails(channelId));
    }

    public NodesPage nodes() {
        return new NodesPage(dataService.createNodeList());
    }

    public NodesPage nodes(List<OpenChannelDto> channels) {
        return new NodesPage(dataService.createNodeList(channels));
    }

    public NodeDetailsPage nodeDetails(Pubkey pubkey) {
        return new NodeDetailsPage(dataService.getNodeDetails(pubkey));
    }

    public ErrorPage error(String errorMessage) {
        return new ErrorPage(errorMessage);
    }
}
