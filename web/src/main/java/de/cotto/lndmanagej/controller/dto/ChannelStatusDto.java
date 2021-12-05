package de.cotto.lndmanagej.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.cotto.lndmanagej.model.ChannelStatus;

public record ChannelStatusDto(
        @JsonProperty("private") boolean privateChannel,
        boolean active,
        boolean closed,
        String openClosed
) {
    public static ChannelStatusDto createFromModel(ChannelStatus status) {
        return new ChannelStatusDto(
                status.privateChannel(),
                status.active(),
                status.closed(),
                status.openCloseStatus().toString()
        );
    }
}
