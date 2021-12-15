package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.statistics.StatisticsFixtures.BALANCES;
import static de.cotto.lndmanagej.statistics.StatisticsFixtures.BALANCES_OLD;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class StatisticsRepositoryIT {
    @Autowired
    private StatisticsRepository repository;

    @Test
    void save() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_not_found() {
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID.getShortChannelId()))
                .isEmpty();
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc() {
        repository.save(BalancesJpaDto.fromModel(BALANCES));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID.getShortChannelId()))
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
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID.getShortChannelId()))
                .map(BalancesJpaDto::toModel).contains(BALANCES);
    }
}