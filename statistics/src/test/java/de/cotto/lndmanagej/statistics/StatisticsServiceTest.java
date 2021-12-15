package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.service.ChannelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_MORE_BALANCE_2;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now(ZoneOffset.UTC);
    @InjectMocks
    private StatisticsService statisticsService;

    @Mock
    private ChannelService channelService;

    @Mock
    private StatisticsDao statisticsDao;

    @Test
    void storeBalances_nothing_stored() {
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
    }

    @Test
    void storeBalances_persists_changed_data() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        ChannelId channelId = LOCAL_OPEN_CHANNEL.getId();
        Balances balances = new Balances(LOCAL_DATE_TIME, channelId, BALANCE_INFORMATION_2);
        when(statisticsDao.getMostRecentBalances(channelId)).thenReturn(Optional.of(balances));
        statisticsService.storeBalances();
        verify(statisticsDao).saveBalances(any());
    }

    @Test
    void storeBalances_does_not_persist_unchanged_data() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        ChannelId channelId = LOCAL_OPEN_CHANNEL.getId();
        Balances balances = new Balances(LOCAL_DATE_TIME, channelId, LOCAL_OPEN_CHANNEL.getBalanceInformation());
        when(statisticsDao.getMostRecentBalances(channelId)).thenReturn(Optional.of(balances));
        statisticsService.storeBalances();
        verify(statisticsDao, never()).saveBalances(any());
    }

    private ArgumentMatcher<Balances> withChannelId(LocalOpenChannel channel) {
        return statistics -> statistics.channelId().equals(channel.getId());
    }

    private ArgumentMatcher<Balances> withBalanceInformation(LocalOpenChannel channel) {
        return statistics -> statistics.balanceInformation().equals(channel.getBalanceInformation());
    }
}