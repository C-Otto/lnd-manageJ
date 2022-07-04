package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.controller.dto.FeeReportDto;
import de.cotto.lndmanagej.controller.dto.FlowReportDto;
import de.cotto.lndmanagej.controller.dto.OnChainCostsDto;
import de.cotto.lndmanagej.controller.dto.OnlineReportDto;
import de.cotto.lndmanagej.controller.dto.RatingDto;
import de.cotto.lndmanagej.controller.dto.RebalanceReportDto;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.NodeDetails;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.List;
import java.util.Set;

public record NodeDetailsDto(
        Pubkey node,
        String alias,
        List<ChannelId> channels,
        List<ClosedChannelDto> closedChannels,
        List<ChannelId> waitingCloseChannels,
        List<ChannelId> pendingForceClosingChannels,
        OnChainCostsDto onChainCosts,
        BalanceInformationModel balance,
        OnlineReportDto onlineReport,
        FeeReportDto feeReport,
        FlowReportDto flowReport,
        RebalanceReportDto rebalanceReport,
        Set<String> warnings,
        RatingDto rating
) {
    public static NodeDetailsDto create(NodeDetails nodeDetails, List<ClosedChannelDto> closedChannels) {
        return new NodeDetailsDto(
                nodeDetails.pubkey(),
                nodeDetails.alias(),
                nodeDetails.channels(),
                closedChannels,
                nodeDetails.waitingCloseChannels(),
                nodeDetails.pendingForceClosingChannels(),
                OnChainCostsDto.createFromModel(nodeDetails.onChainCosts()),
                BalanceInformationModel.createFromModel(nodeDetails.balanceInformation()),
                OnlineReportDto.createFromModel(nodeDetails.onlineReport()),
                FeeReportDto.createFromModel(nodeDetails.feeReport()),
                FlowReportDto.createFromModel(nodeDetails.flowReport()),
                RebalanceReportDto.createFromModel(nodeDetails.rebalanceReport()),
                nodeDetails.warnings().descriptions(),
                RatingDto.fromModel(nodeDetails.rating())
        );
    }
}
