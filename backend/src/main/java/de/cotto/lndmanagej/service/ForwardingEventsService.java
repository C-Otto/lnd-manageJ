package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdAndMaxAge;
import de.cotto.lndmanagej.model.ForwardingEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class ForwardingEventsService {
    private final ForwardingEventsDao forwardingEventsDao;
    private final ClosedChannelAwareCache<List<ForwardingEvent>> cacheIncoming;
    private final ClosedChannelAwareCache<List<ForwardingEvent>> cacheOutgoing;

    public ForwardingEventsService(
            ForwardingEventsDao forwardingEventsDao,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        this.forwardingEventsDao = forwardingEventsDao;
        ClosedChannelAwareCache.Builder builder =
                ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .withSoftValues(true)
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10));
        cacheIncoming = builder.build(List.of(), this::getEventsWithIncomingChannelWithoutCache);
        cacheOutgoing = builder.build(List.of(), this::getEventsWithOutgoingChannelWithoutCache);
    }

    @Timed
    public List<ForwardingEvent> getEventsWithIncomingChannel(ChannelId channelId, Duration maxAge) {
        return cacheIncoming.get(new ChannelIdAndMaxAge(channelId, maxAge));
    }

    @Timed
    public List<ForwardingEvent> getEventsWithOutgoingChannel(ChannelId channelId, Duration maxAge) {
        return cacheOutgoing.get(new ChannelIdAndMaxAge(channelId, maxAge));
    }

    private List<ForwardingEvent> getEventsWithIncomingChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        return forwardingEventsDao.getEventsWithIncomingChannel(
                channelIdAndMaxAge.channelId(),
                channelIdAndMaxAge.maxAge()
        );
    }

    private List<ForwardingEvent> getEventsWithOutgoingChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(
                channelIdAndMaxAge.channelId(),
                channelIdAndMaxAge.maxAge()
        );
    }
}
