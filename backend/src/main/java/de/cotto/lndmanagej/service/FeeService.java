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

@Component
public class FeeService {
    private final ForwardingEventsDao forwardingEventsDao;
    private final ChannelService channelService;
    private final LoadingCache<ChannelId, FeeReport> cacheForOpenChannels;
    private final LoadingCache<ChannelId, FeeReport> cacheForClosedChannels;

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

    @Timed
    public FeeReport getFeeReportForPeer(Pubkey pubkey) {
        return new FeeReport(getEarnedFeesForPeer(pubkey), getSourcedFeesForPeer(pubkey));
    }

    @Timed
    public FeeReport getFeeReportForChannel(ChannelId channelId) {
        if (channelService.isClosed(channelId)) {
            return cacheForClosedChannels.get(channelId);
        }
        return cacheForOpenChannels.get(channelId);
    }

    private FeeReport getFeeReportForChannelWithoutCache(ChannelId channelId) {
        return new FeeReport(getEarnedFeesForChannel(channelId), getSourcedFeesForChannel(channelId));
    }

    private Coins getEarnedFeesForPeer(Pubkey peer) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(this::getEarnedFeesForChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForPeer(Pubkey peer) {
        return channelService.getAllChannelsWith(peer).parallelStream()
                .map(Channel::getId)
                .map(this::getSourcedFeesForChannel)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getEarnedFeesForChannel(ChannelId channelId) {
        return forwardingEventsDao.getEventsWithOutgoingChannel(channelId).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

    private Coins getSourcedFeesForChannel(ChannelId channelId) {
        return forwardingEventsDao.getEventsWithIncomingChannel(channelId).parallelStream()
                .map(ForwardingEvent::fees)
                .reduce(Coins.NONE, Coins::add);
    }

}
