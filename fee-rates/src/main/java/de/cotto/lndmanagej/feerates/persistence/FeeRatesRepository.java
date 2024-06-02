package de.cotto.lndmanagej.feerates.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.Stream;

public interface FeeRatesRepository extends JpaRepository<FeeRatesJpaDto, String> {
    Optional<FeeRatesJpaDto> findTopByChannelIdOrderByTimestampDesc(long channelId);

    Stream<FeeRatesJpaDto> findByChannelIdOrderByTimestampDesc(long channelId);
}
