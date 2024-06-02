package de.cotto.lndmanagej.feerates.persistence;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.FEE_RATES;
import static de.cotto.lndmanagej.feerates.persistence.FeeRatesFixtures.FEE_RATES_OLD;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FeeRatesRepositoryIT {
    private static final long SHORT_ID = CHANNEL_ID.getShortChannelId();

    @Autowired
    private FeeRatesRepository repository;

    @Test
    void save() {
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES));
        assertThat(repository.findAll()).hasSize(1);
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_not_found() {
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .isEmpty();
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc() {
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(FeeRatesJpaDto::toModel).contains(FEE_RATES);
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_wrong_channel() {
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(CHANNEL_ID_2.getShortChannelId()))
                .isEmpty();
    }

    @Test
    void findTopByChannelIdOrderByTimestampDesc_returns_most_recent() {
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES));
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES_OLD));
        assertThat(repository.findTopByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(FeeRatesJpaDto::toModel).contains(FEE_RATES);
    }

    @Test
    void findByChannelIdOrderByTimestampDesc_empty() {
        assertThat(repository.findByChannelIdOrderByTimestampDesc(SHORT_ID)).isEmpty();
    }

    @Test
    void findByChannelIdOrderByTimestampDesc() {
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES));
        repository.save(FeeRatesJpaDto.fromModel(FEE_RATES_OLD));
        assertThat(repository.findByChannelIdOrderByTimestampDesc(SHORT_ID))
                .map(FeeRatesJpaDto::toModel)
                .containsExactly(FEE_RATES, FEE_RATES_OLD);
    }
}
