package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDetailsDto(
        ChannelId channelId,
        Pubkey remotePubkey,
        String remoteAlias,
        @JsonProperty("private") boolean privateChannel
) {
}
