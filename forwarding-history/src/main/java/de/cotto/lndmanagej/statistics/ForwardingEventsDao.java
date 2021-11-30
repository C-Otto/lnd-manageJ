package de.cotto.lndmanagej.statistics;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ForwardingEvent;

import java.util.Collection;
import java.util.List;

public interface ForwardingEventsDao {
    void save(Collection<ForwardingEvent> forwardingEvents);

    int getOffset();

    List<ForwardingEvent> getEventsWithOutgoingChannel(ChannelId channelId);

    List<ForwardingEvent> getEventsWithIncomingChannel(ChannelId channelId);
}
