package de.cotto.lndmanagej.ui.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.ClosedChannel;

public record ClosedChannelDto(
        ChannelId channelId,
        CloseType closeType,
        CloseInitiator closeInitiator,
        int closeHeight
) {

    public long getShortChannelId() {
        return channelId.getShortChannelId();
    }

    public static ClosedChannelDto createFromModel(ClosedChannel channel) {
        return new ClosedChannelDto(
                channel.getId(),
                CloseType.getType(channel),
                channel.getCloseInitiator(),
                channel.getCloseHeight()
        );
    }

    @Override
    public String toString() {
        return channelId.toString();
    }
}
