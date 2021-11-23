package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.statistics.StatisticsFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BalancesDaoImplTest {

    @InjectMocks
    private StatisticsDaoImpl statisticsDaoImpl;

    @Mock
    private StatisticsRepository statisticsRepository;

    @Test
    void saveStatistics() {
        statisticsDaoImpl.saveBalances(StatisticsFixtures.BALANCES);
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getTimestamp() == StatisticsFixtures.TIMESTAMP.toEpochSecond(ZoneOffset.UTC)
        ));
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getChannelId() == CHANNEL_ID.getShortChannelId()));
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getLocalBalance() == BALANCE_INFORMATION.localBalance().satoshis()
        ));
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getLocalReserved() == BALANCE_INFORMATION.localReserve().satoshis()
        ));
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getRemoteBalance() == BALANCE_INFORMATION.remoteBalance().satoshis()
        ));
        verify(statisticsRepository).save(argThat(jpaDto ->
                jpaDto.getRemoteReserved() == BALANCE_INFORMATION.remoteReserve().satoshis()
        ));
    }
}