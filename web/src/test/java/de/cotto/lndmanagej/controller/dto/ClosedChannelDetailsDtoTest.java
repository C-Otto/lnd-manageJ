package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.CloseInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_BREACH;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;

class ClosedChannelDetailsDtoTest {

    private static final ClosedChannelDetailsDto DTO =
            new ClosedChannelDetailsDto(CloseInitiator.LOCAL, 987_654, true, false);

    @Test
    void initiator() {
        assertThat(DTO.initiator()).isEqualTo("LOCAL");
    }

    @Test
    void height() {
        assertThat(DTO.height()).isEqualTo(987_654);
    }

    @Test
    void forceClose() {
        assertThat(DTO.force()).isEqualTo(true);
    }

    @Test
    void createFromModel_open() {
        assertThat(ClosedChannelDetailsDto.createFromModel(LOCAL_OPEN_CHANNEL))
                .isEqualTo(ClosedChannelDetailsDto.UNKNOWN);
    }

    @Test
    void createFromModel_closing() {
        assertThat(ClosedChannelDetailsDto.createFromModel(FORCE_CLOSING_CHANNEL))
                .isEqualTo(ClosedChannelDetailsDto.UNKNOWN);
    }

    @Test
    void createFromModel_coop_closed() {
        ClosedChannelDetailsDto expected = new ClosedChannelDetailsDto(
                CLOSED_CHANNEL.getCloseInitiator().toString(),
                CLOSED_CHANNEL.getCloseHeight(),
                false,
                false
        );
        assertThat(ClosedChannelDetailsDto.createFromModel(CLOSED_CHANNEL)).isEqualTo(expected);
    }

    @Test
    void createFromModel_force_closed() {
        ClosedChannelDetailsDto expected = new ClosedChannelDetailsDto(
                FORCE_CLOSED_CHANNEL.getCloseInitiator().toString(),
                FORCE_CLOSED_CHANNEL.getCloseHeight(),
                true,
                false
        );
        assertThat(ClosedChannelDetailsDto.createFromModel(FORCE_CLOSED_CHANNEL)).isEqualTo(expected);
    }

    @Test
    void createFromModel_breach() {
        ClosedChannelDetailsDto expected = new ClosedChannelDetailsDto(
                FORCE_CLOSED_CHANNEL_BREACH.getCloseInitiator().toString(),
                FORCE_CLOSED_CHANNEL_BREACH.getCloseHeight(),
                true,
                true
        );
        assertThat(ClosedChannelDetailsDto.createFromModel(FORCE_CLOSED_CHANNEL_BREACH)).isEqualTo(expected);
    }
}