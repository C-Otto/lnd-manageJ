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
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.ui.dto.BalanceInformationModel;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UiDataServiceImpl extends UiDataService {

    private final StatusController statusController;
    private final WarningsController warningsController;
    private final ChannelController channelController;
    private final NodeController nodeController;
    private final NodeService nodeService;
    private final ChannelService channelService;

    public UiDataServiceImpl(
            ChannelController channelController,
            StatusController statusController,
            WarningsController warningsController,
            NodeController nodeController,
            NodeService nodeService,
            ChannelService channelService
    ) {
        super();
        this.channelController = channelController;
        this.statusController = statusController;
        this.warningsController = warningsController;
        this.nodeController = nodeController;
        this.nodeService = nodeService;
        this.channelService = channelService;
    }

    @Override
    public NodesAndChannelsWithWarningsDto getWarnings() {
        return warningsController.getWarnings();
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        ChannelsDto openChannels = statusController.getOpenChannels();
        return openChannels.channels().stream()
                .map(this::toOpenChannelDto)
                .toList();
    }

    private OpenChannelDto toOpenChannelDto(ChannelId channelId) {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElseThrow();
        Pubkey pubkey = localChannel.getRemotePubkey();
        long capacitySat = localChannel.getCapacity().satoshis();
        String alias = nodeController.getAlias(pubkey);
        PoliciesDto policies = channelController.getPolicies(channelId);
        BalanceInformationDto balance = channelController.getBalance(channelId);
        return new OpenChannelDto(channelId, alias, pubkey, policies, map(balance), capacitySat);
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

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException {
        de.cotto.lndmanagej.controller.dto.ChannelDetailsDto details = channelController.getDetails(channelId);
        return new ChannelDetailsDto(
                channelId,
                details.remotePubkey(),
                details.remoteAlias(),
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
        return new NodeDetailsDto(
                nodeDetails.node(),
                nodeDetails.alias(),
                nodeDetails.channels(),
                nodeDetails.closedChannels(),
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

}
