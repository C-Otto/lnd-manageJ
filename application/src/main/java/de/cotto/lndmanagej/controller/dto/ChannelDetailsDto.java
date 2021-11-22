package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDetailsDto(
        ChannelId channelId,
        String channelIdCompact,
        String channelIdCompactLnd,
        Pubkey remotePubkey,
        String remoteAlias,
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
                localChannel.getId(),
                localChannel.getId().getCompactForm(),
                localChannel.getId().getCompactFormLnd(),
                localChannel.getRemotePubkey(),
                remoteAlias,
                localChannel.isPrivateChannel(),
                BalanceInformationDto.createFrom(balanceInformation),
                onChainCosts
        );
    }
}
