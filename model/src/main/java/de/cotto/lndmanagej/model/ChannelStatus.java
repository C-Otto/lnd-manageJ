package de.cotto.lndmanagej.model;

public record ChannelStatus(
        boolean privateChannel,
        boolean active,
        boolean closed,
        OpenCloseStatus openCloseStatus
) {
}
