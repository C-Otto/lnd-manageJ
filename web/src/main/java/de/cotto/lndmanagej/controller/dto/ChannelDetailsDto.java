package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.OffChainCosts;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDetailsDto(
        String channelIdShort,
        String channelIdCompact,
        String channelIdCompactLnd,
        ChannelPoint channelPoint,
        int openHeight,
        Pubkey remotePubkey,
        OpenInitiator openInitiator,
        String remoteAlias,
        String capacity,
        String totalSent,
        String totalReceived,
        ChannelStatusDto status,
        BalanceInformationDto balance,
        OnChainCostsDto onChainCosts,
        OffChainCostsDto offChainCosts,
        PoliciesDto policies,
        ClosedChannelDetailsDto closeDetails,
        FeeReportDto feeReport,
        String rebalanceSourceAmount,
        String rebalanceTargetAmount
) {
    public ChannelDetailsDto(
            ChannelDto channelDto,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            OffChainCosts offChainCosts,
            PoliciesDto policies,
            FeeReport feeReport,
            Coins rebalanceSourceAmount,
            Coins rebalanceTargetAmount
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
                channelDto.capacity(),
                channelDto.totalSent(),
                channelDto.totalReceived(),
                channelDto.status(),
                BalanceInformationDto.createFromModel(balanceInformation),
                OnChainCostsDto.createFromModel(onChainCosts),
                OffChainCostsDto.createFromModel(offChainCosts),
                policies,
                channelDto.closeDetails(),
                FeeReportDto.createFromModel(feeReport),
                String.valueOf(rebalanceSourceAmount.milliSatoshis()),
                String.valueOf(rebalanceTargetAmount.milliSatoshis())
        );
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            OffChainCosts offChainCosts,
            PoliciesDto policies,
            ClosedChannelDetailsDto closeDetails,
            FeeReport feeReport,
            Coins rebalanceSourceAmount,
            Coins rebalanceTargetAmount
    ) {
        this(
                new ChannelDto(localChannel, closeDetails),
                remoteAlias,
                balanceInformation,
                onChainCosts,
                offChainCosts,
                policies,
                feeReport,
                rebalanceSourceAmount,
                rebalanceTargetAmount
        );
    }
}
