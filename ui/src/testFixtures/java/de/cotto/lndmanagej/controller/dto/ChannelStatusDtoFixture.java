package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelStatus;

import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;

public class ChannelStatusDtoFixture {

    public static final ChannelStatusDto CHANNEL_STATUS_PUBLIC_OPEN = ChannelStatusDto.createFromModel(
            new ChannelStatus(false, true, false, OPEN));

    public static final ChannelStatusDto CHANNEL_STATUS_PRIVATE_OPEN = ChannelStatusDto.createFromModel(
            new ChannelStatus(true, true, false, OPEN));

}
