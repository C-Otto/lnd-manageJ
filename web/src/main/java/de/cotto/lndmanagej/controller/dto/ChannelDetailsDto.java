package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.LocalChannel;
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
        FeeReportDto feeReport
) {
    public ChannelDetailsDto(
            ChannelDto channelDto,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            OffChainCostsDto offChainCosts,
            PoliciesDto policies,
            FeeReport feeReport
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
                offChainCosts,
                policies,
                channelDto.closeDetails(),
                FeeReportDto.createFromModel(feeReport)
        );
    }

    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCosts onChainCosts,
            OffChainCostsDto offChainCosts,
            PoliciesDto policies,
            ClosedChannelDetailsDto closeDetails,
            FeeReport feeReport
    ) {
        this(
                new ChannelDto(localChannel, closeDetails),
                remoteAlias,
                balanceInformation,
                onChainCosts,
                offChainCosts,
                policies,
                feeReport
        );
    }
}
