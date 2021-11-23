package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("private") boolean privateChannel,
        BalanceInformationDto balance,
        OnChainCostsDto onChainCosts
) {
    public ChannelDetailsDto(
            LocalChannel localChannel,
            String remoteAlias,
            BalanceInformation balanceInformation,
            OnChainCostsDto onChainCosts
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
                localChannel.isPrivateChannel(),
                BalanceInformationDto.createFrom(balanceInformation),
                onChainCosts
        );
    }
}
