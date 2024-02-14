package de.cotto.lndmanagej.ui.dto.warning;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ChannelWarningDtoTest {

    @Test
    void channelId() {
        assertThat(CHANNEL_WARNING_DTO.channelId()).isEqualTo(CHANNEL_ID_2);
    }

    @Test
    void description() {
        assertThat(CHANNEL_WARNING_DTO.description())
                .isEqualTo("Channel balance ranged from 1% to 99% in the past 14 days");
    }
}
