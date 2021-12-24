package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.BreachForceClosedChannel;
import de.cotto.lndmanagej.model.CloseInitiator;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ForceClosedChannel;
import de.cotto.lndmanagej.model.LocalChannel;

public record ClosedChannelDetailsDto(String initiator, int height, boolean force, boolean breach) {
    public static final ClosedChannelDetailsDto UNKNOWN =
            new ClosedChannelDetailsDto("", 0, false, false);

    public ClosedChannelDetailsDto(CloseInitiator initiator, int height, boolean force, boolean breach) {
        this(initiator.toString(), height, force, breach);
    }

    public static ClosedChannelDetailsDto createFromModel(LocalChannel localChannel) {
        if (localChannel instanceof ClosedChannel closedChannel) {
            boolean forceClosed = closedChannel instanceof ForceClosedChannel;
            boolean breach = forceClosed && closedChannel instanceof BreachForceClosedChannel;
            return new ClosedChannelDetailsDto(
                    closedChannel.getCloseInitiator(), closedChannel.getCloseHeight(), forceClosed, breach
            );
        } else {
            return UNKNOWN;
        }
    }
}
