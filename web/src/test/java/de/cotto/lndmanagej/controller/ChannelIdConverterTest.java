package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyNoInteractions;
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

    @Nested
    class TryToConvert {
        @Test
        void valid_short_channel_id() {
            assertThat(channelIdConverter.tryToConvert(String.valueOf(CHANNEL_ID.getShortChannelId())))
                    .contains(CHANNEL_ID);
        }

        @Test
        void just_numbers_small_number() {
            assertThat(channelIdConverter.tryToConvert("123")).isEmpty();
        }

        @Test
        void just_numbers_too_early() {
            assertThat(channelIdConverter.tryToConvert("430103660018532351")).isEmpty();
        }

        @Test
        void compact_form_valid() {
            assertThat(channelIdConverter.tryToConvert(CHANNEL_ID_COMPACT)).contains(CHANNEL_ID);
        }

        @Test
        void compact_form_too_early() {
            assertThat(channelIdConverter.tryToConvert("300000x123x1")).isEmpty();
        }

        @Test
        void compact_lnd_form_valid() {
            assertThat(channelIdConverter.tryToConvert(CHANNEL_ID.getCompactFormLnd())).contains(CHANNEL_ID);
        }

        @Test
        void compact_lnd_form_too_early() {
            assertThat(channelIdConverter.tryToConvert("300000:123:1")).isEmpty();
        }

        @Test
        void channel_point_valid() {
            when(channelIdResolver.resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
            assertThat(channelIdConverter.tryToConvert(CHANNEL_POINT.toString())).contains(CHANNEL_ID);
        }

        @Test
        void channel_point_not_found() {
            assertThat(channelIdConverter.tryToConvert(CHANNEL_POINT.toString())).isEmpty();
        }

        @Test
        void channel_point_lookalike() {
            assertThat(channelIdConverter.tryToConvert("abc:1")).isEmpty();
            verifyNoInteractions(channelIdResolver);
        }

        @Test
        void weird_string() {
            assertThat(channelIdConverter.tryToConvert("[123$!12123.. peter")).isEmpty();
        }

        @Test
        void empty_string() {
            assertThat(channelIdConverter.tryToConvert("[123$!12123.. peter")).isEmpty();
        }
    }
}
