package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.ForwardingEvent;

import java.util.Collection;

public interface ForwardingEventsDao {
    void save(Collection<ForwardingEvent> forwardingEvents);

    int getOffset();
}
