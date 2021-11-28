package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
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
class ChannelIdConverterTest {
    @InjectMocks
    private ChannelIdConverter channelIdConverter;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @Test
    void convert() {
        assertThat(channelIdConverter.convert(CHANNEL_ID.toString())).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_compact_form_with_x() {
        assertThat(channelIdConverter.convert("712345x123x1")).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_compact_form() {
        assertThat(channelIdConverter.convert("712345:123:1")).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_channel_point() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        assertThat(channelIdConverter.convert(CHANNEL_POINT.toString())).isEqualTo(CHANNEL_ID);
    }

    @Test
    void convert_from_channel_point_failure() {
        when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.empty());
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> channelIdConverter.convert(CHANNEL_POINT.toString()));
    }
}