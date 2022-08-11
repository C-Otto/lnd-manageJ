package de.cotto.lndmanagej.ui.page;

import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.WarningService;
import de.cotto.lndmanagej.ui.controller.param.SortBy;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.warning.DashboardWarningDto;
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

import static de.cotto.lndmanagej.ui.controller.param.SortBy.NODE_ALIAS;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("PMD.TooManyMethods")
@Component
public class PageService {

    private final UiDataService dataService;
    private final WarningService warningService;

    public PageService(UiDataService dataService, WarningService warningService) {
        this.dataService = dataService;
        this.warningService = warningService;
    }

    public DashboardPage dashboard(SortBy sortBy) {
        return new DashboardPage(
                sortChannels(dataService.getOpenChannels(), sortBy),
                sortNodes(dataService.createNodeList(), sortBy),
                sortWarnings(warningService.getWarnings())
        );
    }

    private List<DashboardWarningDto> sortWarnings(List<DashboardWarningDto> warnings) {
        return warnings.stream()
                .sorted(Comparator.comparing(DashboardWarningDto::numberOfWarningItems)
                        .reversed().thenComparing(DashboardWarningDto::pubkey))
                .toList();
    }

    public ChannelsPage channels(SortBy sortBy) {
        return new ChannelsPage(sortChannels(dataService.getOpenChannels(), sortBy));
    }

    public ChannelDetailsPage channelDetails(ChannelId channelId) throws NotFoundException {
        return new ChannelDetailsPage(dataService.getChannelDetails(channelId));
    }

    public NodesPage nodes(SortBy sortBy) {
        return new NodesPage(sortNodes(dataService.createNodeList(), sortBy));
    }

    public NodesPage nodes(List<OpenChannelDto> channels, SortBy sortBy) {
        Set<Pubkey> pubkeys = channels.stream().map(OpenChannelDto::remotePubkey).collect(toSet());
        return new NodesPage(sortNodes(dataService.createNodeList(pubkeys), sortBy));
    }

    public NodeDetailsPage nodeDetails(Pubkey pubkey) {
        return new NodeDetailsPage(dataService.getNodeDetails(pubkey));
    }

    public ErrorPage error(String errorMessage) {
        return new ErrorPage(errorMessage);
    }

    private List<OpenChannelDto> sortChannels(List<OpenChannelDto> channels, SortBy sortBy) {
        return channels.stream()
                .sorted(channelComparator(sortBy).thenComparing(OpenChannelDto::channelId))
                .toList();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static Comparator<OpenChannelDto> channelComparator(SortBy sortBy) {
        return switch (sortBy) {
            case ANNOUNCED -> Comparator.comparing(OpenChannelDto::privateChannel);
            case INBOUND -> Comparator.comparingLong(c -> c.balanceInformation().remoteBalanceSat());
            case OUTBOUND -> Comparator.comparingLong(c -> c.balanceInformation().localBalanceSat());
            case CAPACITY -> Comparator.comparing(OpenChannelDto::capacitySat);
            case LOCAL_BASE_FEE -> Comparator.comparing(c -> Long.parseLong(c.policies().local().baseFeeMilliSat()));
            case LOCAL_FEE_RATE -> Comparator.comparing(c -> c.policies().local().feeRatePpm());
            case REMOTE_BASE_FEE -> Comparator.comparing(c -> Long.parseLong(c.policies().remote().baseFeeMilliSat()));
            case REMOTE_FEE_RATE -> Comparator.comparing(c -> c.policies().remote().feeRatePpm());
            case ALIAS -> Comparator.comparing(OpenChannelDto::remoteAlias, String.CASE_INSENSITIVE_ORDER);
            case RATING -> Comparator.comparing(OpenChannelDto::rating);
            case CHANNEL_ID -> Comparator.comparing(OpenChannelDto::channelId);
            default -> Comparator.comparing(c -> c.balanceInformation().getOutboundPercentage());
        };
    }

    private List<NodeDto> sortNodes(List<NodeDto> nodes, SortBy sortBy) {
        return nodes.stream()
                .sorted(nodeComparator(sortBy).thenComparing(NodeDto::pubkey))
                .toList();
    }

    private static Comparator<NodeDto> nodeComparator(SortBy sortBy) {
        return switch (sortBy) {
            case NODE_RATING -> Comparator.comparing(NodeDto::rating);
            case NODE_ALIAS -> Comparator.comparing(NodeDto::alias, String.CASE_INSENSITIVE_ORDER);
            case PUBKEY -> Comparator.comparing(NodeDto::pubkey);
            default -> Comparator.comparing(NodeDto::online).thenComparing(nodeComparator(NODE_ALIAS));
        };
    }
}
