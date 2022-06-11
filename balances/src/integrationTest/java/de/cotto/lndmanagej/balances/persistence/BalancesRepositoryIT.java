package de.cotto.lndmanagej.balances.persistence;

import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static de.cotto.lndmanagej.balances.BalancesFixtures.BALANCES;
import static de.cotto.lndmanagej.balances.BalancesFixtures.BALANCES_OLD;
import static de.cotto.lndmanagej.balances.BalancesFixtures.TIMESTAMP;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.LOCAL_RESERVE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_BALANCE;
import static de.cotto.lndmanagej.model.BalanceInformationFixtures.REMOTE_RESERVE;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@DataJpaTest
class BalancesRepositoryIT {
    private static final long SHORT_ID = CHANNEL_ID.getShortChannelId();

    @Autowired
    private BalancesRepository repository;

    @Test
    void save() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_not_found() {
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .isEmpty();
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(BalancesJpaDto::toModel).contains(BALANCES);
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_wrong_channel() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID_2.getShortChannelId()))
                .isEmpty();
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_returns_most_recent() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        repository.save(BalancesJpaDto.fromModel(BALANCES_OLD));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(BalancesJpaDto::toModel).contains(BALANCES);
    }

    @Test
    void minimum_empty() {
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(
                CHANNEL_ID.getShortChannelId(),
                14
        )).isEmpty();
    }

    @Test
    void minimum() {
        assumeThat(localBalance(BALANCES) < localBalance(BALANCES_OLD)).isTrue();
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        repository.save(BalancesJpaDto.fromModel(BALANCES_OLD));
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(
                CHANNEL_ID.getShortChannelId(),
                14
        )).map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance)
                .contains(BALANCES.balanceInformation().localBalance());
    }

    @Test
    void minimum_too_old() {
        LocalDateTime now = TIMESTAMP;
        Balances balancesMoreThanMinimum = createBalances(LOCAL_BALANCE.add(Coins.ofSatoshis(1)), now);
        Balances balancesMinimumButOld = createBalances(LOCAL_BALANCE, now.minusDays(1));
        long timestamp = balancesMinimumButOld.timestamp().toEpochSecond(ZoneOffset.UTC);

        repository.save(BalancesJpaDto.fromModel(balancesMinimumButOld));
        repository.save(BalancesJpaDto.fromModel(balancesMoreThanMinimum));
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(SHORT_ID, timestamp))
                .map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance)
                .contains(balancesMoreThanMinimum.balanceInformation().localBalance());
    }

    @Test
    void maximum_empty() {
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(SHORT_ID, 14))
                .isEmpty();
    }

    @Test
    void maximum() {
        assumeThat(localBalance(BALANCES_OLD) > localBalance(BALANCES)).isTrue();
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        repository.save(BalancesJpaDto.fromModel(BALANCES_OLD));
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(SHORT_ID, 14))
                .map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance)
                .contains(BALANCES_OLD.balanceInformation().localBalance());
    }

    @Test
    void maximum_too_old() {
        LocalDateTime now = TIMESTAMP;
        Balances balancesLessThanMaximum = createBalances(LOCAL_BALANCE.subtract(Coins.ofSatoshis(1)), now);
        Balances balancesMaximumButOld = createBalances(LOCAL_BALANCE, now.minusDays(1));
        long timestamp = balancesMaximumButOld.timestamp().toEpochSecond(ZoneOffset.UTC);

        repository.save(BalancesJpaDto.fromModel(balancesMaximumButOld));
        repository.save(BalancesJpaDto.fromModel(balancesLessThanMaximum));
        assertThat(repository.findTopByChannelIdAndTimestampAfterOrderByLocalBalance(SHORT_ID, timestamp))
                .map(BalancesJpaDto::toModel)
                .map(Balances::balanceInformation)
                .map(BalanceInformation::localBalance)
                .contains(balancesLessThanMaximum.balanceInformation().localBalance());
    }

    @Test
    void findByChannelIdOrderByTimestampDesc_empty() {
        assertThat(repository.findByChannelIdOrderByTimestampDesc(SHORT_ID)).isEmpty();
    }

    @Test
    void findByChannelIdOrderByTimestampDesc() {
        assumeThat(localBalance(BALANCES_OLD) > localBalance(BALANCES)).isTrue();
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        repository.save(BalancesJpaDto.fromModel(BALANCES_OLD));
        assertThat(repository.findByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(BalancesJpaDto::toModel)
                .containsExactly(BALANCES, BALANCES_OLD);
    }

    private Balances createBalances(Coins localBalance, LocalDateTime timestamp) {
        BalanceInformation balanceInformation =
                new BalanceInformation(localBalance, LOCAL_RESERVE, REMOTE_BALANCE, REMOTE_RESERVE);
        return new Balances(timestamp, CHANNEL_ID, balanceInformation);
    }

    private long localBalance(Balances balances) {
        return balances.balanceInformation().localBalance().milliSatoshis();
    }
}
