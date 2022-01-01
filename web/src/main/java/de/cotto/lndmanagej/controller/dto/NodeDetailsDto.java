package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.NodeDetails;
import de.cotto.lndmanagej.model.NodeWarning;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.List;

public record NodeDetailsDto(
        @JsonSerialize(using = ToStringSerializer.class) Pubkey node,
        String alias,
        List<ChannelId> channels,
        List<ChannelId> closedChannels,
        List<ChannelId> waitingCloseChannels,
        List<ChannelId> pendingForceClosingChannels,
        OnChainCostsDto onChainCosts,
        BalanceInformationDto balance,
        OnlineReportDto onlineReport,
        FeeReportDto feeReport,
        FlowReportDto flowReport,
        RebalanceReportDto rebalanceReport,
        List<NodeWarning> nodeWarnings
) {
    public static NodeDetailsDto createFromModel(NodeDetails nodeDetails) {
        return new NodeDetailsDto(
                nodeDetails.pubkey(),
                nodeDetails.alias(),
                nodeDetails.channels(),
                nodeDetails.closedChannels(),
                nodeDetails.waitingCloseChannels(),
                nodeDetails.pendingForceClosingChannels(),
                OnChainCostsDto.createFromModel(nodeDetails.onChainCosts()),
                BalanceInformationDto.createFromModel(nodeDetails.balanceInformation()),
                OnlineReportDto.createFromModel(nodeDetails.onlineReport()),
                FeeReportDto.createFromModel(nodeDetails.feeReport()),
                FlowReportDto.createFromModel(nodeDetails.flowReport()),
                RebalanceReportDto.createFromModel(nodeDetails.rebalanceReport()),
                nodeDetails.nodeWarnings().warnings()
        );
    }
}
