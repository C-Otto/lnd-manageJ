package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.service.warnings.ChannelWarningsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningFixtures.CHANNEL_BALANCE_FLUCTUATION_WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelWarningsServiceTest {
    private ChannelWarningsService channelWarningsService;

    @Mock
    private ChannelWarningsProvider provider1;

    @Mock
    private ChannelWarningsProvider provider2;

    @Mock
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        channelWarningsService = new ChannelWarningsService(Set.of(provider1, provider2), channelService);
    }

    @Test
    void getChannelWarnings_for_one_channel_no_warning() {
        assertThat(channelWarningsService.getChannelWarnings(CHANNEL_ID_4)).isEqualTo(ChannelWarnings.NONE);
    }

    @Test
    void getChannelWarnings_for_one_channel() {
        when(provider1.getChannelWarnings(CHANNEL_ID))
                .thenReturn(Stream.of(CHANNEL_BALANCE_FLUCTUATION_WARNING));
        ChannelWarnings expected = new ChannelWarnings(
                CHANNEL_BALANCE_FLUCTUATION_WARNING
        );
        assertThat(channelWarningsService.getChannelWarnings(CHANNEL_ID)).isEqualTo(expected);
    }

    @Test
    void getChannelWarnings_channel_no_open_channel() {
        when(channelService.getOpenChannels()).thenReturn(Set.of());
        assertThat(channelWarningsService.getChannelWarnings()).isEmpty();
    }

    @Test
    void getNodeWarnings_no_warnings() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        assertThat(channelWarningsService.getChannelWarnings()).isEmpty();
    }

    @Test
    void getChannelWarnings() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        when(provider1.getChannelWarnings(CHANNEL_ID)).thenReturn(Stream.of(CHANNEL_BALANCE_FLUCTUATION_WARNING));
        when(provider1.getChannelWarnings(CHANNEL_ID_4)).thenReturn(Stream.of());
        assertThat(channelWarningsService.getChannelWarnings()).containsExactlyInAnyOrderEntriesOf(Map.of(
                LOCAL_OPEN_CHANNEL, new ChannelWarnings(CHANNEL_BALANCE_FLUCTUATION_WARNING)
        ));
    }
}
