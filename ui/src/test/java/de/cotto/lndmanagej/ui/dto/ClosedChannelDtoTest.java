package de.cotto.lndmanagej.ui.dto;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CloseInitiator.REMOTE;
import static de.cotto.lndmanagej.model.ClosedChannelFixtures.CLOSE_HEIGHT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.ui.dto.CloseType.COOP_CLOSE;
import static de.cotto.lndmanagej.ui.dto.ClosedChannelDtoFixture.CLOSED_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;

class ClosedChannelDtoTest {

    @Test
    void getShortChannelId() {
        assertThat(CLOSED_CHANNEL_DTO.getShortChannelId()).isEqualTo(CHANNEL_ID.getShortChannelId());
    }

    @Test
    void channelId() {
        assertThat(CLOSED_CHANNEL_DTO.channelId()).isEqualTo(CHANNEL_ID);
    }

    @Test
    void closeHeight() {
        assertThat(CLOSED_CHANNEL_DTO.closeHeight()).isEqualTo(CLOSE_HEIGHT);
    }

    @Test
    void closeType() {
        assertThat(CLOSED_CHANNEL_DTO.closeType()).isEqualTo(COOP_CLOSE);
    }

    @Test
    void closeInitiator() {
        assertThat(CLOSED_CHANNEL_DTO.closeInitiator()).isEqualTo(REMOTE);
    }

    @Test
    void createFromModel() {
        assertThat(ClosedChannelDto.createFromModel(CLOSED_CHANNEL)).isEqualTo(CLOSED_CHANNEL_DTO);
    }

    @Test
    void testToString() {
        assertThat(CLOSED_CHANNEL_DTO.toString()).isEqualTo(CHANNEL_ID.toString());
    }
}