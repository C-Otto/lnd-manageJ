package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.forwardinghistory.ForwardingEventsDao;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.ForwardingEvent;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Period;

@Component
public class FeeService {
    private static final Period DEFAULT_MAX_AGE = Period.ofYears(Integer.MAX_VALUE);
    private final ForwardingEventsDao forwardingEventsDao;
    private final ChannelService channelService;
    private final LoadingCache<CacheKey, FeeReport> cacheForOpenChannels;
    private final LoadingCache<CacheKey, FeeReport> cacheForClosedChannels;

    public FeeService(ForwardingEventsDao forwardingEventsDao, ChannelService channelService) {
        this.forwardingEventsDao = forwardingEventsDao;
        this.channelService = channelService;
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
    public FeeReport getFeeReportForPeer(Pubkey pubkey, Period maxAge) {
        return new FeeReport(getEarnedFeesForPeer(pubkey, maxAge), getSourcedFeesForPeer(pubkey, maxAge));
    }

    public FeeReport getFeeReportForChannel(ChannelId channelId) {
        return getFeeReportForChannel(channelId, DEFAULT_MAX_AGE);
    }

    @Timed
    public FeeReport getFeeReportForChannel(ChannelId channelId, Period maxAge) {
        CacheKey cacheKey = new CacheKey(channelId, maxAge);
        if (channelService.isClosed(channelId)) {
            return cacheForClosedChannels.get(cacheKey);
        }
        return cacheForOpenChannels.get(cacheKey);
    }

    private FeeReport getFeeReportForChannelWithoutCache(CacheKey cacheKey) {
        return new FeeReport(
                getEarnedFeesForChannel(cacheKey.channelId(), cacheKey.maxAge()),
                getSourcedFeesForChannel(cacheKey.channelId(), cacheKey.maxAge())
        );
    }

    private Coins getEarnedFeesForPeer(Pubkey peer, Period maxAge) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getEarnedFeesForChannel(channelId, maxAge))
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForPeer(Pubkey peer, Period maxAge) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(channelId -> getSourcedFeesForChannel(channelId, maxAge))
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getEarnedFeesForChannel(ChannelId channelId, Period maxAge) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(channelId, maxAge).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForChannel(ChannelId channelId, Period maxAge) {
        return forwardingEventsDao.getEventsWithIncomingChannel(channelId, maxAge).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    @SuppressWarnings("UnusedVariable")
    private record CacheKey(ChannelId channelId, Period maxAge) {
    }
}
