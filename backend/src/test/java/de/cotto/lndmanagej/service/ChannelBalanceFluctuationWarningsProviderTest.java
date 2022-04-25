package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.warnings.ChannelBalanceFluctuationWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.annotation.Nullable;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
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
        when(configurationService.getChannelFluctuationWarningLowerThreshold())
                .thenReturn(Optional.of(DEFAULT_LOWER_THRESHOLD - 2));
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void uses_upper_threshold_from_configuration_service() {
        when(configurationService.getChannelFluctuationWarningUpperThreshold())
                .thenReturn(Optional.of(DEFAULT_UPPER_THRESHOLD + 2));
        mockMinMax(DEFAULT_LOWER_THRESHOLD - 1, DEFAULT_UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    private void mockMinMax(@Nullable Integer min, @Nullable Integer max) {
        Coins capacity = LOCAL_OPEN_CHANNEL.getCapacity();
        if (min == null) {
            when(balanceService.getLocalBalanceMinimum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.empty());
        } else {
            Coins minLocalAvailable = Coins.ofSatoshis((long) (capacity.satoshis() / 100.0 * min));
            when(balanceService.getLocalBalanceMinimum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.of(minLocalAvailable));
        }
        if (max == null) {
            when(balanceService.getLocalBalanceMaximum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.empty());
        } else {
            Coins maxLocalAvailable = Coins.ofSatoshis((long) (capacity.satoshis() / 100.0 * max));
            when(balanceService.getLocalBalanceMaximum(CHANNEL_ID, DAYS))
                    .thenReturn(Optional.of(maxLocalAvailable));
        }
    }
}