package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.warnings.ChannelBalanceFluctuationWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.CHANNEL_FLUCTUATION_IGNORE_CHANNEL;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.CHANNEL_FLUCTUATION_LOWER_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.CHANNEL_FLUCTUATION_UPPER_THRESHOLD;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChannelBalanceFluctuationWarningsProviderTest {
    private static final int DEFAULT_LOWER_THRESHOLD = 10;
    private static final int DEFAULT_UPPER_THRESHOLD = 90;
    private static final int DAYS = 14;

    @InjectMocks
    private ChannelBalanceFluctuationWarningsProvider warningsProvider;

    @Mock
    private ChannelService channelService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    @SuppressWarnings("unused")
    private ChannelIdParser channelIdParser;

    @BeforeEach
    void setUp() {
        lenient().when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
    }

    @Test
    void getChannelWarnings_open_channel_not_found() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.empty());
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_both_within_bounds() {
        mockMinMax(DEFAULT_LOWER_THRESHOLD, DEFAULT_UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_minimum_not_found() {
        mockMinMax(null, DEFAULT_UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_maximum_not_found() {
        mockMinMax(DEFAULT_LOWER_THRESHOLD, null);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_minimum_and_maximum_not_found() {
        mockMinMax(null, null);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_just_below_minimum() {
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_just_above_maximum() {
        mockMinMax(DEFAULT_LOWER_THRESHOLD, DEFAULT_UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings() {
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        ChannelBalanceFluctuationWarning expectedWarning =
                new ChannelBalanceFluctuationWarning(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1, DAYS);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).contains(expectedWarning);
    }

    @Test
    void uses_lower_threshold_from_configuration_service() {
        when(configurationService.getIntegerValue(CHANNEL_FLUCTUATION_LOWER_THRESHOLD))
                .thenReturn(Optional.of(DEFAULT_LOWER_THRESHOLD - 2));
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void uses_upper_threshold_from_configuration_service() {
        when(configurationService.getIntegerValue(CHANNEL_FLUCTUATION_UPPER_THRESHOLD))
                .thenReturn(Optional.of(DEFAULT_UPPER_THRESHOLD + 2));
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Nested
    class IgnoredWarnings {
        @BeforeEach
        void setUp() {
            mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        }

        @Test
        void no_warning_for_ignored_channel() {
            when(configurationService.getChannelIds(eq(CHANNEL_FLUCTUATION_IGNORE_CHANNEL), any()))
                    .thenReturn(Set.of(CHANNEL_ID_3, CHANNEL_ID));
            assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
        }

        @Test
        void warning_if_other_channel_is_ignored() {
            when(configurationService.getChannelIds(eq(CHANNEL_FLUCTUATION_IGNORE_CHANNEL), any()))
                    .thenReturn(Set.of(CHANNEL_ID_2, CHANNEL_ID_3));
            assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isNotEmpty();
        }
    }

    private void mockMinMax(@Nullable Integer min, @Nullable Integer max) {
        Coins capacity = LOCAL_OPEN_CHANNEL.getCapacity();
        if (min == null) {
            when(balanceService.getLocalBalanceMinimum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.empty());
        } else {
            Coins minLocalAvailable = Coins.ofSatoshis((long) (capacity.satoshis() / 100.0 * min));
            lenient().when(balanceService.getLocalBalanceMinimum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.of(minLocalAvailable));
        }
        if (max == null) {
            when(balanceService.getLocalBalanceMaximum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.empty());
        } else {
            Coins maxLocalAvailable = Coins.ofSatoshis((long) (capacity.satoshis() / 100.0 * max));
            lenient().when(balanceService.getLocalBalanceMaximum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.of(maxLocalAvailable));
        }
    }
}
