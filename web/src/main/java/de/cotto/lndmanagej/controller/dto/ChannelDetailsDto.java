package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelDetails;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Policies;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;

import java.util.Set;

public record ChannelDetailsDto(
        String channelIdShort,
        String channelIdCompact,
        String channelIdCompactLnd,
        ChannelPoint channelPoint,
        int openHeight,
        Pubkey remotePubkey,
        OpenInitiator openInitiator,
        String remoteAlias,
        String capacitySat,
        String totalSentSat,
        String totalReceivedSat,
        ChannelStatusDto status,
        BalanceInformationDto balance,
        OnChainCostsDto onChainCosts,
        PoliciesDto policies,
        ClosedChannelDetailsDto closeDetails,
        FeeReportDto feeReport,
        FlowReportDto flowReport,
        RebalanceReportDto rebalanceReport,
        long numUpdates,
        Set<String> warnings
) {
    public ChannelDetailsDto(
            ChannelDto channelDto,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            Policies policies,
            FeeReport feeReport,
            FlowReport flowReport,
            RebalanceReport rebalanceReport,
            ChannelWarnings channelWarnings
    ) {
        this(
                channelDto.channelIdShort(),
                channelDto.channelIdCompact(),
                channelDto.channelIdCompactLnd(),
                channelDto.channelPoint(),
                channelDto.openHeight(),
                channelDto.remotePubkey(),
                channelDto.openInitiator(),
                remoteAlias,
                channelDto.capacitySat(),
                channelDto.totalSentSat(),
                channelDto.totalReceivedSat(),
                channelDto.status(),
                BalanceInformationDto.createFromModel(balanceInformation),
                OnChainCostsDto.createFromModel(onChainCosts),
                PoliciesDto.createFromModel(policies),
                channelDto.closeDetails(),
                FeeReportDto.createFromModel(feeReport),
                FlowReportDto.createFromModel(flowReport),
                RebalanceReportDto.createFromModel(rebalanceReport),
                channelDto.numUpdates(),
                channelWarnings.descriptions()
        );
    }

    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            Policies policies,
            FeeReport feeReport,
            FlowReport flowReport,
            RebalanceReport rebalanceReport,
            ChannelWarnings channelWarnings
    ) {
        this(
                new ChannelDto(localChannel),
                remoteAlias,
                balanceInformation,
                onChainCosts,
                policies,
                feeReport,
                flowReport,
                rebalanceReport,
                channelWarnings
        );
    }

    public static ChannelDetailsDto createFromModel(ChannelDetails channelDetails) {
        return new ChannelDetailsDto(
                channelDetails.localChannel(),
                channelDetails.remoteAlias(),
                channelDetails.balanceInformation(),
                channelDetails.onChainCosts(),
                channelDetails.policies(),
                channelDetails.feeReport(),
                channelDetails.flowReport(),
                channelDetails.rebalanceReport(),
                channelDetails.warnings()
        );
    }
}
