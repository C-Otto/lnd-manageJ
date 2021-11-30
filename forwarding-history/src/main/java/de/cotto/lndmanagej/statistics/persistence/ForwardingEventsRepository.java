package de.cotto.lndmanagej.statistics.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ForwardingEventsRepository extends JpaRepository<ForwardingEventJpaDto, Long> {
    @Query("SELECT max(index) FROM ForwardingEventJpaDto")
    Optional<Integer> findMaxIndex();
}
