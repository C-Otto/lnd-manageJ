package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;

public record ChannelDetailsDto(
        ChannelId channelId,
        String channelIdCompact,
        String channelIdCompactLnd,
        Pubkey remotePubkey,
        String remoteAlias,
        @JsonProperty("private") boolean privateChannel
) {
    public ChannelDetailsDto(ChannelId channelId, Pubkey remotePubkey, String remoteAlias, boolean privateChannel) {
        this(
                channelId,
                channelId.getCompactForm(),
                channelId.getCompactFormLnd(),
                remotePubkey,
                remoteAlias,
                privateChannel
        );
    }
}
