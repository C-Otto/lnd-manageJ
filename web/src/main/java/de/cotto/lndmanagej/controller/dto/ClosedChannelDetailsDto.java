package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.CloseInitiator;

public record ClosedChannelDetailsDto(String initiator, int height) {
    public static final ClosedChannelDetailsDto UNKNOWN = new ClosedChannelDetailsDto("", 0);

    public ClosedChannelDetailsDto(CloseInitiator initiator, int height) {
        this(initiator.toString(), height);
    }
}
