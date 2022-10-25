package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelIdParserTest {
    @InjectMocks
    private ChannelIdParser channelIdParser;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @Test
    void parseFromString() {
        assertThat(channelIdParser.parseFromString(CHANNEL_ID.toString())).isEqualTo(CHANNEL_ID);
    }

    @Test
    void parseFromString_from_short_channel_id() {
        String shortChannelId = String.valueOf(CHANNEL_ID.getShortChannelId());
        assertThat(channelIdParser.parseFromString(shortChannelId)).isEqualTo(CHANNEL_ID);
    }

    @Test
    void parseFromString_from_compact_form_with_x() {
        assertThat(channelIdParser.parseFromString("712345x123x1")).isEqualTo(CHANNEL_ID);
    }

    @Test
    void parseFromString_from_compact_form() {
        assertThat(channelIdParser.parseFromString("712345:123:1")).isEqualTo(CHANNEL_ID);
    }

    @Test
    void parseFromString_from_channel_point() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        assertThat(channelIdParser.parseFromString(CHANNEL_POINT.toString())).isEqualTo(CHANNEL_ID);
    }

    @Test
    void parseFromString_from_channel_point_failure() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.empty());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> channelIdParser.parseFromString(CHANNEL_POINT.toString()));
    }
}
