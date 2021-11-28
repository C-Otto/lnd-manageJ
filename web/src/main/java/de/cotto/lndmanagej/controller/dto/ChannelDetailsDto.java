package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.LocalChannel;
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
        PoliciesDto policies,
        ClosedChannelDetailsDto closeDetails
) {
    public ChannelDetailsDto(
            ChannelDto channelDto,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCostsDto onChainCosts,
            PoliciesDto policies,
            ClosedChannelDetailsDto closeDetails
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
                BalanceInformationDto.createFrom(balanceInformation),
                onChainCosts,
                policies,
                closeDetails
        );
    }

    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCostsDto onChainCosts,
            PoliciesDto policies,
            ClosedChannelDetailsDto closeDetails
    ) {
        this(
                new ChannelDto(localChannel),
                remoteAlias,
                balanceInformation,
                onChainCosts,
                policies,
                closeDetails
        );
    }
}
