package de.cotto.lndmanagej.service;

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
    private static final int LOWER_THRESHOLD = 10;
    private static final int UPPER_THRESHOLD = 90;
    private static final int DAYS = 14;

    @InjectMocks
    private ChannelBalanceFluctuationWarningsProvider warningsProvider;

    @Mock
    private ChannelService channelService;

    @Mock
    private BalanceService balanceService;

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
        mockMinMax(LOWER_THRESHOLD, UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_minimum_not_found() {
        mockMinMax(null, UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_maximum_not_found() {
        mockMinMax(LOWER_THRESHOLD, null);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_minimum_and_maximum_not_found() {
        mockMinMax(null, null);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_just_below_minimum() {
        mockMinMax(LOWER_THRESHOLD - 1, UPPER_THRESHOLD);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings_just_above_maximum() {
        mockMinMax(LOWER_THRESHOLD, UPPER_THRESHOLD + 1);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getChannelWarnings() {
        mockMinMax(LOWER_THRESHOLD - 1, UPPER_THRESHOLD + 1);
        ChannelBalanceFluctuationWarning expectedWarning =
                new ChannelBalanceFluctuationWarning(LOWER_THRESHOLD - 1, UPPER_THRESHOLD + 1, DAYS);
        assertThat(warningsProvider.getChannelWarnings(CHANNEL_ID)).contains(expectedWarning);
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