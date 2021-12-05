package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelStatus;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelStatusDtoTest {
    private final ChannelStatusDto dtoOpen =
            new ChannelStatusDto(false, true, false, "OPEN");
    private final ChannelStatus channelStatusOpen = new ChannelStatus(false, true, false, OPEN);

    @Test
    void createFromModel() {
        assertThat(ChannelStatusDto.createFromModel(channelStatusOpen)).isEqualTo(dtoOpen);
    }
}