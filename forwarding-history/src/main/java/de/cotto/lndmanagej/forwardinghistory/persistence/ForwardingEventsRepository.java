package de.cotto.lndmanagej.forwardinghistory.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ForwardingEventsRepository extends JpaRepository<ForwardingEventJpaDto, Long> {
    @Query("SELECT max(eventIndex) FROM ForwardingEventJpaDto")
    Optional<Integer> findMaxIndex();

    List<ForwardingEventJpaDto> findByChannelIncomingAndTimestampGreaterThan(long channelId, long after);

    List<ForwardingEventJpaDto> findByChannelOutgoingAndTimestampGreaterThan(long channelId, long after);
}
