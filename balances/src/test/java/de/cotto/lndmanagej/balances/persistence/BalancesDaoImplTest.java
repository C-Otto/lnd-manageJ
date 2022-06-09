package de.cotto.lndmanagej.balances.persistence;

import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;

import static de.cotto.lndmanagej.balances.BalancesFixtures.BALANCES;
import static de.cotto.lndmanagej.balances.BalancesFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.longThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalancesDaoImplTest {

    @InjectMocks
    private BalancesDaoImpl dao;

    @Mock
    private BalancesRepository balancesRepository;

    @Test
    void saveStatistics() {
        dao.saveBalances(BALANCES);
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getTimestamp() == TIMESTAMP.toEpochSecond(ZoneOffset.UTC)
        ));
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getChannelId() == CHANNEL_ID.getShortChannelId()));
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getLocalBalance() == BALANCE_INFORMATION.localBalance().satoshis()
        ));
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getLocalReserved() == BALANCE_INFORMATION.localReserve().satoshis()
        ));
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getRemoteBalance() == BALANCE_INFORMATION.remoteBalance().satoshis()
        ));
        verify(balancesRepository).save(argThat(jpaDto ->
                jpaDto.getRemoteReserved() == BALANCE_INFORMATION.remoteReserve().satoshis()
        ));
    }

    @Test
    void getMostRecentBalance_not_found() {
        assertThat(dao.getMostRecentBalances(CHANNEL_ID)).isEmpty();
    }

    @Test
    void getMostRecentBalance() {
        when(balancesRepository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID.getShortChannelId()))
                .thenReturn(Optional.of(BalancesJpaDto.fromModel(BALANCES)));
        assertThat(dao.getMostRecentBalances(CHANNEL_ID)).contains(BALANCES);
    }

    @Test
    void getLocalBalanceMinimum_empty() {
        assertThat(dao.getLocalBalanceMinimum(CHANNEL_ID, 7)).isEmpty();
    }

    @Test
    void getLocalBalanceMinimum() {
        int days = 14;
        Coins localBalance = Coins.ofSatoshis(456);
        Balances balances = getWithLocalBalance(localBalance);
        when(balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(
                eq(CHANNEL_ID.getShortChannelId()),
                anyLong()
        )).thenReturn(Optional.of(BalancesJpaDto.fromModel(balances)));
        assertThat(dao.getLocalBalanceMinimum(CHANNEL_ID, days)).contains(localBalance);

        long expectedTimestamp = ZonedDateTime.now(ZoneOffset.UTC).minusDays(days).toEpochSecond();
        verify(balancesRepository).findTopByChannelIdAndTimestampAfterOrderByLocalBalance(anyLong(),
                longThat(timestamp -> timestamp > 0.95 * expectedTimestamp && timestamp < 1.05 * expectedTimestamp)
        );
    }

    @Test
    void getLocalBalanceMaximum_empty() {
        assertThat(dao.getLocalBalanceMaximum(CHANNEL_ID, 7)).isEmpty();
    }

    @Test
    void getLocalBalanceMaximum() {
        int days = 14;
        Coins localBalance = Coins.ofSatoshis(456);
        Balances balances = getWithLocalBalance(localBalance);
        when(balancesRepository.findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(
                eq(CHANNEL_ID.getShortChannelId()),
                anyLong()
        )).thenReturn(Optional.of(BalancesJpaDto.fromModel(balances)));
        assertThat(dao.getLocalBalanceMaximum(CHANNEL_ID, days)).contains(localBalance);

        long expectedTimestamp = ZonedDateTime.now(ZoneOffset.UTC).minusDays(days).toEpochSecond();
        verify(balancesRepository).findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(anyLong(),
                longThat(timestamp -> timestamp > 0.95 * expectedTimestamp && timestamp < 1.05 * expectedTimestamp)
        );
    }

    @Test
    void getLocalBalanceAverage_empty() {
        assertThat(dao.getLocalBalanceAverage(CHANNEL_ID, 1)).isEmpty();
    }

    @Test
    void getLocalBalanceAverage() {
        int days = 14;
        Coins averageLocalBalance = Coins.ofSatoshis(456);
        when(balancesRepository.getAverageLocalBalance(eq(CHANNEL_ID.getShortChannelId()), anyLong()))
                .thenReturn(Optional.of(averageLocalBalance.satoshis()));
        assertThat(dao.getLocalBalanceAverage(CHANNEL_ID, days)).contains(averageLocalBalance);

        long expectedTimestamp = ZonedDateTime.now(ZoneOffset.UTC).minusDays(days).toEpochSecond();
        verify(balancesRepository).getAverageLocalBalance(anyLong(),
                longThat(timestamp -> timestamp > 0.95 * expectedTimestamp && timestamp < 1.05 * expectedTimestamp)
        );
    }

    private Balances getWithLocalBalance(Coins localBalance) {
        BalanceInformation balanceInformation =
                new BalanceInformation(localBalance, LOCAL_RESERVE, REMOTE_BALANCE, REMOTE_RESERVE);
        return new Balances(TIMESTAMP, CHANNEL_ID, balanceInformation);
    }
}
