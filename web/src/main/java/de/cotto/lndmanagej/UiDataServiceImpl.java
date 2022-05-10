package de.cotto.lndmanagej;

import de.cotto.lndmanagej.controller.ChannelController;
import de.cotto.lndmanagej.controller.NodeController;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.controller.StatusController;
import de.cotto.lndmanagej.controller.WarningsController;
import de.cotto.lndmanagej.controller.dto.BalanceInformationDto;
import de.cotto.lndmanagej.controller.dto.ChannelsDto;
import de.cotto.lndmanagej.controller.dto.NodeDetailsDto;
import de.cotto.lndmanagej.controller.dto.PoliciesDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
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
            WarningsController warningsController, NodeController nodeController,
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
    public StatusModel getStatus() {
        return new StatusModel(
                statusController.isSyncedToChain(),
                statusController.getBlockHeight(),
                warningsController.getWarnings()
        );
    }

    @Override
    public List<OpenChannelDto> getOpenChannels() {
        ChannelsDto openChannels = statusController.getOpenChannels();
        return openChannels.channels().stream().map(this::toOpenChannelDto).toList();
    }

    private OpenChannelDto toOpenChannelDto(ChannelId channelId) {
        LocalChannel localChannel = channelService.getLocalChannel(channelId).orElseThrow();
        Pubkey remotePubkey = localChannel.getRemotePubkey();
        String alias = nodeController.getAlias(remotePubkey);
        PoliciesDto policies = channelController.getPolicies(channelId);
        BalanceInformationDto balance = channelController.getBalance(channelId);
        return new OpenChannelDto(channelId, alias, remotePubkey, policies, balance);
    }

    @Override
    public ChannelDetailsDto getChannelDetails(ChannelId channelId) throws NotFoundException {
        de.cotto.lndmanagej.controller.dto.ChannelDetailsDto details = channelController.getDetails(channelId);
        return new ChannelDetailsDto(
                channelId,
                details.remotePubkey(),
                details.remoteAlias(),
                details.openInitiator(),
                details.balance(),
                details.onChainCosts(),
                details.policies(),
                details.feeReport(),
                details.flowReport(),
                details.rebalanceReport(),
                details.warnings());
    }

    @Override
    public NodeDto getNode(Pubkey pubkey) {
        Node node = nodeService.getNode(pubkey);
        return new NodeDto(node.pubkey().toString(), node.alias(), node.online());
    }

    @Override
    public NodeDetailsDto getNodeDetails(Pubkey pubkey) {
        return nodeController.getDetails(pubkey);
    }

}
