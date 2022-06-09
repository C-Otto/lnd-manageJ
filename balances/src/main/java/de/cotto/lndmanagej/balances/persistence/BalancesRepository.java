package de.cotto.lndmanagej.balances.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BalancesRepository extends JpaRepository<BalancesJpaDto, String> {
    Optional<BalancesJpaDto> findTopByChannelIdOrderByTimestampDesc(long channelId);

    Optional<BalancesJpaDto> findTopByChannelIdAndTimestampAfterOrderByLocalBalance(long channelId, long timestamp);

    Optional<BalancesJpaDto> findTopByChannelIdAndTimestampAfterOrderByLocalBalanceDesc(long channelId, long timestamp);

    @Query("SELECT avg(localBalance) FROM BalancesJpaDto b WHERE b.channelId = ?1 AND b.timestamp > ?2")
    Optional<Long> getAverageLocalBalance(long channelId, long timestamp);
}
