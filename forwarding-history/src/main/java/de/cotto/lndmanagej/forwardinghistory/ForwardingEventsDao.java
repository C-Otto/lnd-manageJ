package de.cotto.lndmanagej.forwardinghistory;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ForwardingEvent;

import java.time.Period;
import java.util.Collection;
import java.util.List;

public interface ForwardingEventsDao {
    void save(Collection<ForwardingEvent> forwardingEvents);

    int getOffset();

    List<ForwardingEvent> getEventsWithOutgoingChannel(ChannelId channelId, Period maxAge);

    List<ForwardingEvent> getEventsWithIncomingChannel(ChannelId channelId, Period maxAge);
}
