package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelStatus;
import de.cotto.lndmanagej.model.OpenInitiator;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.OpenCloseStatus.OPEN;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class ChannelDtoTest {
    private static final ClosedChannelDetailsDto CLOSE_DETAILS = new ClosedChannelDetailsDto("abc", 123);
    private static final ChannelDto CHANNEL_DTO = new ChannelDto(CLOSED_CHANNEL, CLOSE_DETAILS);

    @Test
    void channelIdShort() {
        assertThat(CHANNEL_DTO.channelIdShort()).isEqualTo(String.valueOf(CHANNEL_ID.getShortChannelId()));
    }

    @Test
    void channelIdCompact() {
        assertThat(CHANNEL_DTO.channelIdCompact()).isEqualTo(CHANNEL_ID.getCompactForm());
    }

    @Test
    void channelIdCompactLnd() {
        assertThat(CHANNEL_DTO.channelIdCompactLnd()).isEqualTo(CHANNEL_ID.getCompactFormLnd());
    }

    @Test
    void channelPoint() {
        assertThat(CHANNEL_DTO.channelPoint()).isEqualTo(CHANNEL_POINT);
    }

    @Test
    void openHeight() {
        assertThat(CHANNEL_DTO.openHeight()).isEqualTo(CHANNEL_ID.getBlockHeight());
    }

    @Test
    void remotePubkey() {
        assertThat(CHANNEL_DTO.remotePubkey()).isEqualTo(PUBKEY_2);
    }

    @Test
    void capacity() {
        assertThat(CHANNEL_DTO.capacity()).isEqualTo(String.valueOf(CAPACITY.satoshis()));
    }

    @Test
    void totalSent() {
        assertThat(new ChannelDto(LOCAL_OPEN_CHANNEL, CLOSE_DETAILS).totalSent())
                .isEqualTo(String.valueOf(TOTAL_SENT.satoshis()));
    }

    @Test
    void totalReceived() {
        assertThat(new ChannelDto(LOCAL_OPEN_CHANNEL, CLOSE_DETAILS).totalReceived())
                .isEqualTo(String.valueOf(TOTAL_RECEIVED.satoshis()));
    }

    @Test
    void openInitiator() {
        assertThat(CHANNEL_DTO.openInitiator()).isEqualTo(OpenInitiator.LOCAL);
    }

    @Test
    void closeDetails() {
        assertThat(CHANNEL_DTO.closeDetails()).isEqualTo(CLOSE_DETAILS);
    }

    @Test
    void status() {
        ChannelDto dto = new ChannelDto(LOCAL_OPEN_CHANNEL, CLOSE_DETAILS);
        ChannelStatusDto channelStatusDto =
                ChannelStatusDto.createFrom(new ChannelStatus(false, true, false, OPEN));
        assertThat(dto.status()).isEqualTo(channelStatusDto);
    }
}