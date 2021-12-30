package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class FeeService {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private final ForwardingEventsDao forwardingEventsDao;
    private final ChannelService channelService;
    private final OwnNodeService ownNodeService;
    private final LoadingCache<CacheKey, FeeReport> cacheForOpenChannels;
    private final LoadingCache<CacheKey, FeeReport> cacheForClosedChannels;

    public FeeService(
            ForwardingEventsDao forwardingEventsDao,
            ChannelService channelService,
            OwnNodeService ownNodeService
    ) {
        this.forwardingEventsDao = forwardingEventsDao;
        this.channelService = channelService;
        this.ownNodeService = ownNodeService;
        cacheForOpenChannels = new CacheBuilder()
                .withRefresh(Duration.ofSeconds(5))
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getFeeReportForChannelWithoutCache);
        cacheForClosedChannels = new CacheBuilder()
                .withRefresh(Duration.ofHours(12))
                .withExpiry(Duration.ofHours(24))
                .build(this::getFeeReportForChannelWithoutCache);
    }

    public FeeReport getFeeReportForPeer(Pubkey pubkey) {
        return getFeeReportForPeer(pubkey, DEFAULT_MAX_AGE);
    }

    @Timed
    public FeeReport getFeeReportForPeer(Pubkey pubkey, Duration maxAge) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getFeeReportForChannel(channelId, maxAge))
                .reduce(FeeReport.EMPTY, FeeReport::add);
    }

    public FeeReport getFeeReportForChannel(ChannelId channelId) {
        return getFeeReportForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public FeeReport getFeeReportForChannel(ChannelId channelId, Duration maxAge) {
        CacheKey cacheKey = new CacheKey(channelId, maxAge);
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            return cacheForOpenChannels.get(cacheKey);
        }
        if (isClosedLongerThan(closedChannel, maxAge)) {
            return FeeReport.EMPTY;
        }
        return cacheForClosedChannels.get(cacheKey);
    }

    private FeeReport getFeeReportForChannelWithoutCache(CacheKey cacheKey) {
        return new FeeReport(
                getEarnedFeesForChannel(cacheKey.channelId(), cacheKey.maxAge()),
                getSourcedFeesForChannel(cacheKey.channelId(), cacheKey.maxAge())
        );
    }

    private Coins getEarnedFeesForChannel(ChannelId channelId, Duration maxAge) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(channelId, maxAge).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForChannel(ChannelId channelId, Duration maxAge) {
        return forwardingEventsDao.getEventsWithIncomingChannel(channelId, maxAge).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    private boolean isClosedLongerThan(ClosedChannel closedChannel, Duration maxAge) {
        int blocksSinceClose = ownNodeService.getBlockHeight() - closedChannel.getCloseHeight();
        int daysClosedWithSafetyMargin = (int) (0.5 * blocksSinceClose * 10.0 / 60 / 24);
        return maxAge.minus(Duration.ofDays(daysClosedWithSafetyMargin)).isNegative();
    }

    @SuppressWarnings("UnusedVariable")
    private record CacheKey(ChannelId channelId, Duration maxAge) {
    }
}
