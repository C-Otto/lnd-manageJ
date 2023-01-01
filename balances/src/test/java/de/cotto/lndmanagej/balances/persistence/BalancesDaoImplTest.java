package de.cotto.lndmanagej.balances.persistence;

import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoinsAndDuration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.stream.Stream;

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

    @Nested
    class GetLocalBalanceAverage {

        private static final int DAYS = 14;

        @Test
        void empty() {
            assertThat(dao.getLocalBalanceAverageOpenChannel(CHANNEL_ID, 1)).isEmpty();
        }

        @Test
        void one_entry() {
            Balances balances = getWithLocalBalance(Coins.ofSatoshis(100), 10);
            CoinsAndDuration expected = new CoinsAndDuration(
                    balances.balanceInformation().localBalance(),
                    Duration.ofMinutes(10)
            );
            when(balancesRepository.findByChannelIdOrderByTimestampDesc(eq(CHANNEL_ID.getShortChannelId())))
                    .thenReturn(Stream.of(BalancesJpaDto.fromModel(balances)));
            assertThat(dao.getLocalBalanceAverageOpenChannel(CHANNEL_ID, DAYS)).contains(expected);
        }

        @Test
        void two_entries_same_duration() {
            Balances balances1 = getWithLocalBalance(Coins.ofSatoshis(100), 10);
            Balances balances2 = getWithLocalBalance(Coins.ofSatoshis(300), 20);
            when(balancesRepository.findByChannelIdOrderByTimestampDesc(anyLong()))
                    .thenReturn(Stream.of(BalancesJpaDto.fromModel(balances1), BalancesJpaDto.fromModel(balances2)));
            CoinsAndDuration expected = new CoinsAndDuration(Coins.ofSatoshis(200), Duration.ofMinutes(20));
            assertThat(dao.getLocalBalanceAverageOpenChannel(CHANNEL_ID, DAYS)).contains(expected);
        }

        @Test
        void one_old_entry() {
            Balances balances = getWithLocalBalance(Coins.ofSatoshis(300), DAYS + 85 * 24 * 60);
            when(balancesRepository.findByChannelIdOrderByTimestampDesc(anyLong()))
                    .thenReturn(Stream.of(BalancesJpaDto.fromModel(balances)));
            CoinsAndDuration expected = new CoinsAndDuration(
                    balances.balanceInformation().localBalance(),
                    Duration.ofMinutes(DAYS * 24 * 60)
            );
            assertThat(dao.getLocalBalanceAverageOpenChannel(CHANNEL_ID, DAYS)).contains(expected);
        }

        @Test
        void two_entries_one_very_old() {
            Balances balances1 = getWithLocalBalance(Coins.ofSatoshis(100), 7 * 24 * 60);
            Balances balances2 = getWithLocalBalance(Coins.ofSatoshis(300), 99 * 24 * 60);
            CoinsAndDuration expected = new CoinsAndDuration(
                    Coins.ofSatoshis(199),
                    Duration.ofMinutes(DAYS * 24 * 60 - 1)
            );
            when(balancesRepository.findByChannelIdOrderByTimestampDesc(anyLong()))
                    .thenReturn(Stream.of(BalancesJpaDto.fromModel(balances1), BalancesJpaDto.fromModel(balances2)));
            assertThat(dao.getLocalBalanceAverageOpenChannel(CHANNEL_ID, DAYS)).contains(expected);
        }

        @Test
        void old_entries_then_closed() {
            Balances balances1 = getWithLocalBalance(Coins.ofSatoshis(1), 80);
            Balances balances2 = getWithLocalBalance(Coins.ofSatoshis(1_000_000), 90);
            Balances balances3 = getWithLocalBalance(Coins.ofSatoshis(2_000_000), 100);
            CoinsAndDuration expected = new CoinsAndDuration(
                    Coins.ofSatoshis(1_500_000),
                    Duration.ofMinutes(100 - 80)
            );
            when(balancesRepository.findByChannelIdOrderByTimestampDesc(eq(CHANNEL_ID.getShortChannelId())))
                    .thenReturn(Stream.of(
                            BalancesJpaDto.fromModel(balances1),
                            BalancesJpaDto.fromModel(balances2),
                            BalancesJpaDto.fromModel(balances3)
                    ));
            assertThat(dao.getLocalBalanceAverageClosedChannel(CHANNEL_ID, DAYS)).contains(expected);
        }
    }

    private Balances getWithLocalBalance(Coins localBalance) {
        return getWithLocalBalance(localBalance, 0);
    }

    private Balances getWithLocalBalance(Coins localBalance, int ageInMinutes) {
        BalanceInformation balanceInformation =
                new BalanceInformation(localBalance, LOCAL_RESERVE, REMOTE_BALANCE, REMOTE_RESERVE);
        return new Balances(TIMESTAMP.minusMinutes(ageInMinutes), CHANNEL_ID, balanceInformation);
    }
}
