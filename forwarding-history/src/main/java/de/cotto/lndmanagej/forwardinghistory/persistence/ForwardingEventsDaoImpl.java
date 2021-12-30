package de.cotto.lndmanagej.forwardinghistory.persistence;

import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ForwardingEvent;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

@Component
@Transactional
public class ForwardingEventsDaoImpl implements ForwardingEventsDao {
    private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1_000;
    private final ForwardingEventsRepository repository;

    public ForwardingEventsDaoImpl(ForwardingEventsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Collection<ForwardingEvent> forwardingEvents) {
        List<ForwardingEventJpaDto> converted = forwardingEvents.stream()
                .map(ForwardingEventJpaDto::createFromModel)
                .toList();
        repository.saveAll(converted);
    }

    @Override
    public int getOffset() {
        return repository.findMaxIndex().orElse(0);
    }

    @Override
    public List<ForwardingEvent> getEventsWithOutgoingChannel(ChannelId channelId, Period maxAge) {
        return repository.findByChannelOutgoingAndTimestampGreaterThan(
                        channelId.getShortChannelId(),
                        getAfterEpochMilliSeconds(maxAge)
                ).stream()
                .map(ForwardingEventJpaDto::toModel)
                .toList();
    }

    @Override
    public List<ForwardingEvent> getEventsWithIncomingChannel(ChannelId channelId, Period maxAge) {
        return repository.findByChannelIncomingAndTimestampGreaterThan(
                        channelId.getShortChannelId(),
                        getAfterEpochMilliSeconds(maxAge)
                ).stream()
                .map(ForwardingEventJpaDto::toModel)
                .toList();
    }

    private long getAfterEpochMilliSeconds(Period maxAge) {
        return Instant.now().toEpochMilli() - maxAge.get(ChronoUnit.DAYS) * MILLISECONDS_PER_DAY;
    }
}
