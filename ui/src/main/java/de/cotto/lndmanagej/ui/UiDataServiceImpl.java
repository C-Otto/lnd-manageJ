package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelController;
import de.cotto.lndmanagej.controller.NodeController;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.StatusController;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Rating;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import de.cotto.lndmanagej.service.RatingService;
import de.cotto.lndmanagej.ui.dto.BalanceInformationModel;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.CloseType;
import de.cotto.lndmanagej.ui.dto.ClosedChannelDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@SuppressWarnings("PMD.ExcessiveImports")
@Component
public class UiDataServiceImpl extends UiDataService {

    private final StatusController statusController;
    private final ChannelController channelController;
    private final NodeController nodeController;
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final RatingService ratingService;

    public UiDataServiceImpl(
            ChannelController channelController,
            StatusController statusController,
            NodeController nodeController,
            NodeService nodeService,
            ChannelService channelService,
            OwnNodeService ownNodeService,
            RatingService ratingService
    ) {
        super();
        this.channelController = channelController;
        this.statusController = statusController;
        this.nodeController = nodeController;
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        this.ratingService = ratingService;
    }

    @Override
    public Set<Pubkey> getPubkeys() {
        return channelService.getOpenChannels().stream().map(LocalChannel::getRemotePubkey).collect(toSet());
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        ChannelsDto openChannels = statusController.getOpenChannels();
        return openChannels.channels().parallelStream()
                .map(this::toOpenChannelDto)
                .toList();
    }

    private OpenChannelDto toOpenChannelDto(ChannelId channelId) {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElseThrow();
        return new OpenChannelDto(
                channelId,
                nodeController.getAlias(localChannel.getRemotePubkey()),
                localChannel.getRemotePubkey(),
                channelController.getPolicies(channelId),
                map(channelController.getBalance(channelId)),
                localChannel.getCapacity().satoshis(),
                localChannel.getStatus().privateChannel(),
                ratingService.getRatingForChannel(channelId).orElse(Rating.EMPTY).value()
        );
    }

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException {
        de.cotto.lndmanagej.controller.dto.ChannelDetailsDto details = channelController.getDetails(channelId);
        int channelAgeInDays = calculateDaysOfBlocks(ownNodeService.getBlockHeight(), details.openHeight());
        return new ChannelDetailsDto(
                channelId,
                details.remotePubkey(),
                details.remoteAlias(),
                channelAgeInDays,
                details.status(),
                details.openInitiator(),
                map(details.balance()),
                Long.parseLong(details.capacitySat()),
                details.onChainCosts(),
                details.policies(),
                details.feeReport(),
                details.flowReport(),
                details.rebalanceReport(),
                details.warnings(),
                details.rating()
        );
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        Node node = nodeService.getNode(pubkey);
        Rating rating = ratingService.getRatingForPeer(pubkey);
        return new NodeDto(node.pubkey().toString(), node.alias(), node.online(), rating.value());
    }

    @Override
    public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
        de.cotto.lndmanagej.controller.dto.NodeDetailsDto nodeDetails = nodeController.getDetails(pubkey);
        Set<ClosedChannel> closedChannels = channelService.getClosedChannelsWith(pubkey);
        return new NodeDetailsDto(
                nodeDetails.node(),
                nodeDetails.alias(),
                nodeDetails.channels(),
                map(closedChannels),
                nodeDetails.waitingCloseChannels(),
                nodeDetails.pendingForceClosingChannels(),
                nodeDetails.onChainCosts(),
                map(nodeDetails.balance()),
                nodeDetails.onlineReport(),
                nodeDetails.feeReport(),
                nodeDetails.flowReport(),
                nodeDetails.rebalanceReport(),
                nodeDetails.warnings(),
                nodeDetails.rating()
        );
    }

    private BalanceInformationModel map(BalanceInformationDto balance) {
        return new BalanceInformationModel(
                Long.parseLong(balance.localBalanceSat()),
                Long.parseLong(balance.localReserveSat()),
                Long.parseLong(balance.localAvailableSat()),
                Long.parseLong(balance.remoteBalanceSat()),
                Long.parseLong(balance.remoteReserveSat()),
                Long.parseLong(balance.remoteAvailableSat())
        );
    }

    private List<ClosedChannelDto> map(Set<ClosedChannel> channels) {
        return channels.stream()
                .map(this::toClosedChannelDto)
                .toList();
    }

    private ClosedChannelDto toClosedChannelDto(ClosedChannel channel) {
        return new ClosedChannelDto(
                channel.getId(),
                CloseType.getType(channel),
                channel.getCloseInitiator(),
                channel.getCloseHeight()
        );
    }

}
