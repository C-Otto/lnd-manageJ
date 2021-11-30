package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.statistics.ForwardingEventsDao;
import org.springframework.stereotype.Component;

@Component
public class FeeService {
    private final ForwardingEventsDao forwardingEventsDao;

    public FeeService(ForwardingEventsDao forwardingEventsDao) {
        this.forwardingEventsDao = forwardingEventsDao;
    }

    public Coins getEarnedFeesForChannel(ChannelId channelId) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(channelId).stream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }
}
