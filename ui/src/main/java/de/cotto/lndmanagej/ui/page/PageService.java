package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.controller.param.SortBy;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class PageService {

    private final UiDataService dataService;

    public PageService(UiDataService dataService) {
        this.dataService = dataService;
    }

    public DashboardPage dashboard(SortBy sort) {
        return new DashboardPage(
                sort(dataService.getOpenChannels(), sort),
                dataService.createNodeList(),
                dataService.getWarnings()
        );
    }

    public ChannelsPage channels(SortBy sortBy) {
        return new ChannelsPage(sort(dataService.getOpenChannels(), sortBy));
    }

    public ChannelDetailsPage channelDetails(ChannelId channelId) throws NotFoundException {
        return new ChannelDetailsPage(dataService.getChannelDetails(channelId));
    }

    public NodesPage nodes() {
        return new NodesPage(dataService.createNodeList());
    }

    public NodesPage nodes(List<OpenChannelDto> channels) {
        Set<Pubkey> pubkeys = channels.stream().map(OpenChannelDto::remotePubkey).collect(toSet());
        return new NodesPage(dataService.createNodeList(pubkeys));
    }

    public NodeDetailsPage nodeDetails(Pubkey pubkey) {
        return new NodeDetailsPage(dataService.getNodeDetails(pubkey));
    }

    public ErrorPage error(String errorMessage) {
        return new ErrorPage(errorMessage);
    }

    private List<OpenChannelDto> sort(List<OpenChannelDto> channels, SortBy sort) {
        return channels.stream()
                .sorted(channelComparator(sort).thenComparing(OpenChannelDto::channelId))
                .toList();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static Comparator<OpenChannelDto> channelComparator(SortBy sort) {
        return switch (sort) {
            case announced -> Comparator.comparing(OpenChannelDto::privateChannel);
            case inbound -> Comparator.comparingLong(c -> c.balanceInformation().remoteBalanceSat());
            case outbound -> Comparator.comparingLong(c -> c.balanceInformation().localBalanceSat());
            case capacity -> Comparator.comparing(OpenChannelDto::capacitySat);
            case localbasefee -> Comparator.comparing(c -> Long.parseLong(c.policies().local().baseFeeMilliSat()));
            case localfeerate -> Comparator.comparing(c -> c.policies().local().feeRatePpm());
            case remotebasefee -> Comparator.comparing(c -> Long.parseLong(c.policies().remote().baseFeeMilliSat()));
            case remotefeerate -> Comparator.comparing(c -> c.policies().remote().feeRatePpm());
            case alias -> Comparator.comparing(OpenChannelDto::remoteAlias, String.CASE_INSENSITIVE_ORDER);
            case channelrating -> Comparator.comparing(OpenChannelDto::rating);
            case channelid -> Comparator.comparing(OpenChannelDto::channelId);
            default -> Comparator.comparing(c -> c.balanceInformation().getOutboundPercentage());
        };
    }
}
