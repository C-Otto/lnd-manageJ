package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_MORE_BALANCE_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalancesServiceTest {
    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private ChannelService channelService;

    @Mock
    private StatisticsDao statisticsDao;

    @Test
    void storeBalances() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(
                LOCAL_OPEN_CHANNEL,
                LOCAL_OPEN_CHANNEL_MORE_BALANCE_2
        ));
        statisticsService.storeBalances();
        verify(statisticsDao).saveBalances(argThat(withBalanceInformation(LOCAL_OPEN_CHANNEL_MORE_BALANCE_2)));
        verify(statisticsDao).saveBalances(argThat(withChannelId(LOCAL_OPEN_CHANNEL_MORE_BALANCE_2)));
        verify(statisticsDao).saveBalances(argThat(withBalanceInformation(LOCAL_OPEN_CHANNEL)));
        verify(statisticsDao).saveBalances(argThat(withChannelId(LOCAL_OPEN_CHANNEL)));
        verify(statisticsDao, times(2)).saveBalances(any());
        verifyNoMoreInteractions(statisticsDao);
    }

    private ArgumentMatcher<Balances> withChannelId(LocalOpenChannel channel) {
        return statistics -> statistics.channelId().equals(channel.getId());
    }

    private ArgumentMatcher<Balances> withBalanceInformation(LocalOpenChannel channel) {
        return statistics -> statistics.balanceInformation().equals(channel.getBalanceInformation());
    }
}