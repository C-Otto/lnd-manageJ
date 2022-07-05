package de.cotto.lndmanagej.ui.dto.warning;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.dto.warning.ChannelWarningDtoFixture.CHANNEL_WARNING_DTO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ChannelWarningDtoTest {

    @Test
    void channelId() {
        assertThat(CHANNEL_WARNING_DTO.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void description() {
        assertThat(CHANNEL_WARNING_DTO.description()).isEqualTo("Channel has accumulated 101,000 updates");
    }
}