package de.cotto.lndmanagej.statistics.persistence;

import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.statistics.ForwardingEventsDao;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
@Transactional
public class ForwardingEventsDaoImpl implements ForwardingEventsDao {
    private final ForwardingEventsRepository repository;

    public ForwardingEventsDaoImpl(ForwardingEventsRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Collection<ForwardingEvent> forwardingEvents) {
        List<ForwardingEventJpaDto> converted = forwardingEvents.stream()
                .map(ForwardingEventJpaDto::createFromForwardingEvent)
                .collect(toList());
        repository.saveAll(converted);
    }

    @Override
    public int getOffset() {
        return repository.findMaxIndex().orElse(0);
    }
}
