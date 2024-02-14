package de.cotto.lndmanagej.ui.dto.warning;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;

public class ChannelWarningDtoFixture {
    public static final ChannelWarningDto CHANNEL_WARNING_DTO =
            new ChannelWarningDto(CHANNEL_ID_2, "Channel balance ranged from 1% to 99% in the past 14 days");

    public static final ChannelWarningDto CHANNEL_WARNING_DTO_2 =
            new ChannelWarningDto(CHANNEL_ID, "Channel balance ranged from 2% to 97% in the past 7 days");
}
