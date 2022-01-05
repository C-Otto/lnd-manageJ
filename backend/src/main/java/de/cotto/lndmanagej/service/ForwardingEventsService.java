package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.ForwardingEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
public class ForwardingEventsService {
    private final ForwardingEventsDao forwardingEventsDao;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheIncomingForOpenChannels;
    private final LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheIncomingForClosedChannels;
    private final LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheOutgoingForOpenChannels;
    private final LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheOutgoingForClosedChannels;

    public ForwardingEventsService(
            ForwardingEventsDao forwardingEventsDao,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        this.forwardingEventsDao = forwardingEventsDao;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        cacheIncomingForOpenChannels = new CacheBuilder()
                .withSoftValues(true)
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getEventsWithIncomingChannelWithoutCache);
        cacheOutgoingForOpenChannels = new CacheBuilder()
                .withSoftValues(true)
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getEventsWithOutgoingChannelWithoutCache);
        cacheIncomingForClosedChannels = new CacheBuilder()
                .withSoftValues(true)
                .withRefresh(Duration.ofHours(12))
                .withExpiry(Duration.ofHours(24))
                .build(this::getEventsWithIncomingChannelWithoutCache);
        cacheOutgoingForClosedChannels = new CacheBuilder()
                .withSoftValues(true)
                .withRefresh(Duration.ofHours(12))
                .withExpiry(Duration.ofHours(24))
                .build(this::getEventsWithOutgoingChannelWithoutCache);
    }

    @Timed
    public List<ForwardingEvent> getEventsWithIncomingChannel(ChannelId channelId, Duration maxAge) {
        return getEvents(channelId, maxAge, cacheIncomingForOpenChannels, cacheIncomingForClosedChannels);
    }

    @Timed
    public List<ForwardingEvent> getEventsWithOutgoingChannel(ChannelId channelId, Duration maxAge) {
        return getEvents(channelId, maxAge, cacheOutgoingForOpenChannels, cacheOutgoingForClosedChannels);
    }

    private List<ForwardingEvent> getEvents(
            ChannelId channelId,
            Duration maxAge,
            LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheOpen,
            LoadingCache<ChannelIdAndMaxAge, List<ForwardingEvent>> cacheClosed
    ) {
        ChannelIdAndMaxAge channelIdAndMaxAge = new ChannelIdAndMaxAge(channelId, maxAge);
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            return cacheOpen.get(channelIdAndMaxAge);
        }
        if (isClosedLongerThan(closedChannel, maxAge)) {
            return List.of();
        }
        return cacheClosed.get(channelIdAndMaxAge);
    }

    private boolean isClosedLongerThan(ClosedChannel closedChannel, Duration maxAge) {
        int blocksSinceClose = ownNodeService.getBlockHeight() - closedChannel.getCloseHeight();
        int daysClosedWithSafetyMargin = (int) (0.5 * blocksSinceClose * 10.0 / 60 / 24);
        return maxAge.minus(Duration.ofDays(daysClosedWithSafetyMargin)).isNegative();
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
