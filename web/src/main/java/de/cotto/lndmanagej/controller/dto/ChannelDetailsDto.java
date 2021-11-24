package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDetailsDto(
        String channelIdShort,
        String channelIdCompact,
        String channelIdCompactLnd,
        ChannelPoint channelPoint,
        int openHeight,
        Pubkey remotePubkey,
        String remoteAlias,
        String capacity,
        ChannelStatusDto status,
        BalanceInformationDto balance,
        OnChainCostsDto onChainCosts,
        FeeConfigurationDto feeConfiguration
) {
    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCostsDto onChainCosts,
            FeeConfigurationDto feeConfiguration
    ) {
        this(
                String.valueOf(localChannel.getId().getShortChannelId()),
                localChannel.getId().getCompactForm(),
                localChannel.getId().getCompactFormLnd(),
                localChannel.getChannelPoint(),
                localChannel.getId().getBlockHeight(),
                localChannel.getRemotePubkey(),
                remoteAlias,
                String.valueOf(localChannel.getCapacity().satoshis()),
                ChannelStatusDto.createFrom(localChannel.getStatus()),
                BalanceInformationDto.createFrom(balanceInformation),
                onChainCosts,
                feeConfiguration
        );
    }
}
