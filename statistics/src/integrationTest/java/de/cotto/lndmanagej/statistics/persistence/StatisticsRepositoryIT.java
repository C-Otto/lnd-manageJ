package de.cotto.lndmanagej.statistics.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.statistics.StatisticsFixtures.BALANCES;
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
}