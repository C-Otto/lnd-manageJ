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
    private final LoadingCache<CacheKey, List<ForwardingEvent>> cacheIncomingForOpenChannels;
    private final LoadingCache<CacheKey, List<ForwardingEvent>> cacheIncomingForClosedChannels;
    private final LoadingCache<CacheKey, List<ForwardingEvent>> cacheOutgoingForOpenChannels;
    private final LoadingCache<CacheKey, List<ForwardingEvent>> cacheOutgoingForClosedChannels;

    public ForwardingEventsService(
            ForwardingEventsDao forwardingEventsDao,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        this.forwardingEventsDao = forwardingEventsDao;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        cacheIncomingForOpenChannels = new CacheBuilder()
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getEventsWithIncomingChannelWithoutCache);
        cacheOutgoingForOpenChannels = new CacheBuilder()
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getEventsWithOutgoingChannelWithoutCache);
        cacheIncomingForClosedChannels = new CacheBuilder()
                .withRefresh(Duration.ofHours(12))
                .withExpiry(Duration.ofHours(24))
                .build(this::getEventsWithIncomingChannelWithoutCache);
        cacheOutgoingForClosedChannels = new CacheBuilder()
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
            LoadingCache<CacheKey, List<ForwardingEvent>> cacheOpen,
            LoadingCache<CacheKey, List<ForwardingEvent>> cacheClosed
    ) {
        CacheKey cacheKey = new CacheKey(channelId, maxAge);
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            return cacheOpen.get(cacheKey);
        }
        if (isClosedLongerThan(closedChannel, maxAge)) {
            return List.of();
        }
        return cacheClosed.get(cacheKey);
    }

    private boolean isClosedLongerThan(ClosedChannel closedChannel, Duration maxAge) {
        int blocksSinceClose = ownNodeService.getBlockHeight() - closedChannel.getCloseHeight();
        int daysClosedWithSafetyMargin = (int) (0.5 * blocksSinceClose * 10.0 / 60 / 24);
        return maxAge.minus(Duration.ofDays(daysClosedWithSafetyMargin)).isNegative();
    }

    private List<ForwardingEvent> getEventsWithIncomingChannelWithoutCache(CacheKey cacheKey) {
        return forwardingEventsDao.getEventsWithIncomingChannel(cacheKey.channelId(), cacheKey.maxAge());
    }

    private List<ForwardingEvent> getEventsWithOutgoingChannelWithoutCache(CacheKey cacheKey) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(cacheKey.channelId(), cacheKey.maxAge());
    }

    @SuppressWarnings("UnusedVariable")
    private record CacheKey(ChannelId channelId, Duration maxAge) {
    }
}
