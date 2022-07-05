package de.cotto.lndmanagej.ui.dto.warning;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;

public class ChannelWarningDtoFixture {
    public static final ChannelWarningDto CHANNEL_WARNING_DTO =
            new ChannelWarningDto(CHANNEL_ID, "Channel has accumulated 101,000 updates");

    public static final ChannelWarningDto CHANNEL_WARNING_DTO_2 =
            new ChannelWarningDto(CHANNEL_ID_2, "Channel balance ranged from 2% to 97% in the past 7 days");
}
