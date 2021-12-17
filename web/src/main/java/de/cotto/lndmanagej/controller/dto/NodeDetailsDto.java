package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.RebalanceReport;

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
        boolean online,
        FeeReportDto feeReport,
        RebalanceReportDto rebalanceReport
) {
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public NodeDetailsDto(
            Pubkey pubkey,
            String alias,
            List<ChannelId> channels,
            List<ChannelId> closedChannels,
            List<ChannelId> waitingCloseChannels,
            List<ChannelId> pendingForceClosingChannels,
            OnChainCosts onChainCosts,
            BalanceInformation balanceInformation,
            boolean online,
            FeeReport feeReport,
            RebalanceReport rebalanceReport
    ) {
        this(
                pubkey,
                alias,
                channels,
                closedChannels,
                waitingCloseChannels,
                pendingForceClosingChannels,
                OnChainCostsDto.createFromModel(onChainCosts),
                BalanceInformationDto.createFromModel(balanceInformation),
                online,
                FeeReportDto.createFromModel(feeReport),
                RebalanceReportDto.createFromModel(rebalanceReport)
        );
    }
}
