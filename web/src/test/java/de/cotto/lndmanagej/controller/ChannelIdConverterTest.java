package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_COMPACT;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelIdConverterTest {
    private ChannelIdConverter channelIdConverter;

    @Mock
    private ChannelIdResolver channelIdResolver;

    @BeforeEach
    void setUp() {
        channelIdConverter = new ChannelIdConverter(new ChannelIdParser(channelIdResolver));
    }

    @Test
    void convert() {
        assertThat(channelIdConverter.convert(CHANNEL_ID.toString())).isEqualTo(CHANNEL_ID);
    }

    @Nested
    class TryToConvert {
        @Test
        void valid_short_channel_id() {
            assertThat(channelIdConverter.tryToConvert(String.valueOf(CHANNEL_ID.getShortChannelId())))
                    .contains(CHANNEL_ID);
        }

        @Test
        void just_numbers_zero() {
            assertThat(channelIdConverter.tryToConvert("0")).isEmpty();
        }

        @Test
        void compact_form_valid() {
            assertThat(channelIdConverter.tryToConvert(CHANNEL_ID_COMPACT)).contains(CHANNEL_ID);
        }

        @Test
        void compact_form_zero() {
            assertThat(channelIdConverter.tryToConvert("0x0x0")).isEmpty();
        }

        @Test
        void compact_lnd_form_valid() {
            assertThat(channelIdConverter.tryToConvert(CHANNEL_ID.getCompactFormLnd())).contains(CHANNEL_ID);
        }

        @Test
        void compact_lnd_form_zero() {
            assertThat(channelIdConverter.tryToConvert("0:0:0")).isEmpty();
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
