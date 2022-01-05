package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.selfpayments.SelfPaymentsDao;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class SelfPaymentsService {
    private static final Duration DEFAULT_MAX_AGE = Duration.ofDays(365 * 1_000);
    private static final Duration EXPIRY = Duration.ofSeconds(30);
    private static final Duration REFRESH = Duration.ofSeconds(15);
    private static final Duration EXPIRY_CLOSED = Duration.ofHours(24);
    private static final Duration REFRESH_CLOSED = Duration.ofHours(12);

    private final SelfPaymentsDao dao;
    private final ChannelService channelService;
    private final LoadingCache<ChannelIdAndMaxAge, List<SelfPayment>> cacheFrom;
    private final LoadingCache<ChannelIdAndMaxAge, List<SelfPayment>> cacheFromClosed;
    private final LoadingCache<ChannelIdAndMaxAge, List<SelfPayment>> cacheTo;
    private final LoadingCache<ChannelIdAndMaxAge, List<SelfPayment>> cacheToClosed;

    public SelfPaymentsService(SelfPaymentsDao dao, ChannelService channelService) {
        this.dao = dao;
        this.channelService = channelService;
        cacheFrom = new CacheBuilder()
                .withSoftValues(true)
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH)
                .build(this::getSelfPaymentsFromChannelWithoutCache);
        cacheTo = new CacheBuilder()
                .withSoftValues(true)
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH)
                .build(this::getSelfPaymentsToChannelWithoutCache);
        cacheFromClosed = new CacheBuilder()
                .withSoftValues(true)
                .withExpiry(EXPIRY_CLOSED)
                .withRefresh(REFRESH_CLOSED)
                .build(this::getSelfPaymentsFromChannelWithoutCache);
        cacheToClosed = new CacheBuilder()
                .withSoftValues(true)
                .withExpiry(EXPIRY_CLOSED)
                .withRefresh(REFRESH_CLOSED)
                .build(this::getSelfPaymentsToChannelWithoutCache);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId) {
        return getSelfPaymentsFromChannel(channelId, DEFAULT_MAX_AGE);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId, Duration maxAge) {
        ChannelIdAndMaxAge channelIdAndMaxAge = new ChannelIdAndMaxAge(channelId, maxAge);
        if (channelService.isClosed(channelId)) {
            return cacheFromClosed.get(channelIdAndMaxAge);
        }
        return cacheFrom.get(channelIdAndMaxAge);
    }

    public List<SelfPayment> getSelfPaymentsFromPeer(Pubkey pubkey) {
        return getSelfPaymentsForAllChannels(pubkey, this::getSelfPaymentsFromChannel);
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId) {
        return getSelfPaymentsToChannel(channelId, DEFAULT_MAX_AGE);
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId, Duration maxAge) {
        ChannelIdAndMaxAge channelIdAndMaxAge = new ChannelIdAndMaxAge(channelId, maxAge);
        if (channelService.isClosed(channelId)) {
            return cacheToClosed.get(channelIdAndMaxAge);
        }
        return cacheTo.get(channelIdAndMaxAge);
    }

    public List<SelfPayment> getSelfPaymentsToPeer(Pubkey pubkey) {
        return getSelfPaymentsForAllChannels(pubkey, this::getSelfPaymentsToChannel);
    }

    private List<SelfPayment> getSelfPaymentsForAllChannels(
            Pubkey pubkey,
            Function<ChannelId, List<SelfPayment>> provider
    ) {
        return channelService.getAllChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(provider)
                .flatMap(List::stream)
                .sorted(Comparator.comparing(SelfPayment::settleDate))
                .toList();
    }

    private List<SelfPayment> getSelfPaymentsFromChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        return dao.getSelfPaymentsFromChannel(channelIdAndMaxAge.channelId(), channelIdAndMaxAge.maxAge()).stream()
                .distinct()
                .toList();
    }

    private List<SelfPayment> getSelfPaymentsToChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        return dao.getSelfPaymentsToChannel(channelIdAndMaxAge.channelId(), channelIdAndMaxAge.maxAge());
    }
}
