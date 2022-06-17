package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelController;
import de.cotto.lndmanagej.controller.NodeController;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.StatusController;
import de.cotto.lndmanagej.controller.WarningsController;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import de.cotto.lndmanagej.ui.dto.BalanceInformationModel;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.CloseType;
import de.cotto.lndmanagej.ui.dto.ClosedChannelDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class UiDataServiceImpl extends UiDataService {

    private final StatusController statusController;
    private final WarningsController warningsController;
    private final ChannelController channelController;
    private final NodeController nodeController;
    private final NodeService nodeService;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;

    public UiDataServiceImpl(
            ChannelController channelController,
            StatusController statusController,
            WarningsController warningsController,
            NodeController nodeController,
            NodeService nodeService,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        super();
        this.channelController = channelController;
        this.statusController = statusController;
        this.warningsController = warningsController;
        this.nodeController = nodeController;
        this.nodeService = nodeService;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
    }

    @Override
    public NodesAndChannelsWithWarningsDto getWarnings() {
        return warningsController.getWarnings();
    }

    @Override
    public Set<Pubkey> getPubkeys() {
        return channelService.getOpenChannels().stream().map(LocalChannel::getRemotePubkey).collect(toSet());
    }

    @Override
    public List<OpenChannelDto> getOpenChannels(@Nullable String sort) {
        ChannelsDto openChannels = statusController.getOpenChannels();
        return sort(openChannels.channels().parallelStream()
                .map(this::toOpenChannelDto)
                .toList(), sort);
    }

    private OpenChannelDto toOpenChannelDto(ChannelId channelId) {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElseThrow();
        Pubkey pubkey = localChannel.getRemotePubkey();
        long capacitySat = localChannel.getCapacity().satoshis();
        boolean privateChannel = localChannel.getStatus().privateChannel();
        String alias = nodeController.getAlias(pubkey);
        PoliciesDto policies = channelController.getPolicies(channelId);
        BalanceInformationDto balance = channelController.getBalance(channelId);
        return new OpenChannelDto(channelId, alias, pubkey, policies, map(balance), capacitySat, privateChannel);
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
                details.warnings()
        );
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        Node node = nodeService.getNode(pubkey);
        return new NodeDto(node.pubkey().toString(), node.alias(), node.online());
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
                nodeDetails.warnings()
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
