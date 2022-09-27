package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.warnings.ChannelNumUpdatesWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.MAX_NUM_UPDATES;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.MAX_NUM_UPDATES_IGNORE_CHANNEL;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelNumUpdatesWarningsProviderTest {
    private static final long DEFAULT_MAX_NUM_UPDATES = 100_000L;

    @InjectMocks
    private ChannelNumUpdatesWarningsProvider warningsProvider;

    @Mock
    private ChannelService channelService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    @SuppressWarnings("unused")
    private ChannelIdParser channelIdParser;

    @Test
    void getChannelWarnings_open_channel_not_found() {
        when(channelService.getOpenChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_low_number_of_num_updates() {
        when(channelService.getOpenChannel(CHANNEL_ID))
                .thenReturn(Optional.of(createChannel(DEFAULT_MAX_NUM_UPDATES)));
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_high_number_of_num_updates() {
        when(channelService.getOpenChannel(CHANNEL_ID))
                .thenReturn(Optional.of(createChannel(DEFAULT_MAX_NUM_UPDATES + 1)));
        ChannelNumUpdatesWarning channelNumUpdatesWarning = new ChannelNumUpdatesWarning(DEFAULT_MAX_NUM_UPDATES + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).containsExactly(channelNumUpdatesWarning);
    }

    @Test
    void getChannelWarnings_uses_configured_value() {
        when(configurationService.getIntegerValue(MAX_NUM_UPDATES)).thenReturn(Optional.of(10));
        when(channelService.getOpenChannel(CHANNEL_ID)).thenReturn(Optional.of(createChannel(11)));
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).hasSize(1);
    }

    @Nested
    class IgnoredWarnings {
        @BeforeEach
        void setUp() {
            lenient().when(channelService.getOpenChannel(CHANNEL_ID))
                    .thenReturn(Optional.of(createChannel(DEFAULT_MAX_NUM_UPDATES + 1)));
        }

        @Test
        void no_warning_for_ignored_channel() {
            when(configurationService.getChannelIds(eq(MAX_NUM_UPDATES_IGNORE_CHANNEL), any()))
                    .thenReturn(Set.of(CHANNEL_ID_2, CHANNEL_ID));
            assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
        }

        @Test
        void warning_if_other_channel_is_ignored() {
            when(configurationService.getChannelIds(eq(MAX_NUM_UPDATES_IGNORE_CHANNEL), any()))
                    .thenReturn(Set.of(CHANNEL_ID_2, CHANNEL_ID_3));
            assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isNotEmpty();
        }
    }

    private LocalOpenChannel createChannel(long numUpdates) {
        return new LocalOpenChannel(
                new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                BALANCE_INFORMATION,
                LOCAL,
                TOTAL_SENT,
                TOTAL_RECEIVED,
                false,
                true,
                numUpdates
        );
    }
}
