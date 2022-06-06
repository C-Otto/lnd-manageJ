package de.cotto.lndmanagej.ui.dto;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CloseInitiator.LOCAL;
import static de.cotto.lndmanagej.model.CloseInitiator.REMOTE;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSE_HEIGHT;
import static de.cotto.lndmanagej.ui.dto.CloseType.BREACH_FORCE_CLOSE;
import static de.cotto.lndmanagej.ui.dto.CloseType.COOP_CLOSE;
import static de.cotto.lndmanagej.ui.dto.CloseType.FORCE_CLOSE;

public class ClosedChannelDtoFixture {

    public static final ClosedChannelDto CLOSED_CHANNEL_DTO
            = new ClosedChannelDto(CHANNEL_ID, COOP_CLOSE, REMOTE, CLOSE_HEIGHT);

    public static final ClosedChannelDto FORCE_CLOSED_CHANNEL_DTO
            = new ClosedChannelDto(CHANNEL_ID, FORCE_CLOSE, LOCAL, CLOSE_HEIGHT);

    public static final ClosedChannelDto BREACH_CLOSED_CHANNEL_DTO
            = new ClosedChannelDto(CHANNEL_ID, BREACH_FORCE_CLOSE, LOCAL, CLOSE_HEIGHT);
}
