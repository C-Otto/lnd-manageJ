package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;

public record ClosedChannelDetailsDto(String initiator, int height, boolean force, boolean breach) {
    public static final ClosedChannelDetailsDto UNKNOWN =
            new ClosedChannelDetailsDto("", 0, false, false);

    public ClosedChannelDetailsDto(CloseInitiator initiator, int height, boolean force, boolean breach) {
        this(initiator.toString(), height, force, breach);
    }

    public static ClosedChannelDetailsDto createFromModel(LocalChannel localChannel) {
        boolean closed = localChannel.isClosed();
        if (closed) {
            ClosedChannel closedChannel = localChannel.getAsClosedChannel();
            boolean forceClosed = closedChannel.isForceClosed();
            boolean breach = forceClosed && closedChannel.getAsForceClosedChannel().isBreach();
            return new ClosedChannelDetailsDto(
                    closedChannel.getCloseInitiator(), closedChannel.getCloseHeight(), forceClosed, breach
            );
        } else {
            return UNKNOWN;
        }
    }
}
