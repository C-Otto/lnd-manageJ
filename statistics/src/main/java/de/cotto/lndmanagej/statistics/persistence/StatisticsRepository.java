package de.cotto.lndmanagej.statistics.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatisticsRepository extends JpaRepository<BalancesJpaDto, String> {
    Optional<BalancesJpaDto> findTopByChannelIdOrderByTimestampDesc(long channelId);
}
