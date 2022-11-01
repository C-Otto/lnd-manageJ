package de.cotto.lndmanagej.ui.dto;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CloseInitiator.REMOTE;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSE_HEIGHT;
import static de.cotto.lndmanagej.ui.dto.CloseType.COOP_CLOSE;

public class ClosedChannelDtoFixture {
    public static final ClosedChannelDto CLOSED_CHANNEL_DTO
            = new ClosedChannelDto(CHANNEL_ID, COOP_CLOSE, REMOTE, CLOSE_HEIGHT);
}
