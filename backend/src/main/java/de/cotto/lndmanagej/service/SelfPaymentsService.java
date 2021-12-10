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
    private static final Duration EXPIRY = Duration.ofSeconds(30);
    private static final Duration REFRESH = Duration.ofSeconds(15);
    private static final Duration EXPIRY_CLOSED = Duration.ofHours(24);
    private static final Duration REFRESH_CLOSED = Duration.ofHours(12);

    private final SelfPaymentsDao dao;
    private final ChannelService channelService;
    private final LoadingCache<ChannelId, List<SelfPayment>> cacheFrom;
    private final LoadingCache<ChannelId, List<SelfPayment>> cacheFromClosed;
    private final LoadingCache<ChannelId, List<SelfPayment>> cacheTo;
    private final LoadingCache<ChannelId, List<SelfPayment>> cacheToClosed;

    public SelfPaymentsService(SelfPaymentsDao dao, ChannelService channelService) {
        this.dao = dao;
        this.channelService = channelService;
        cacheFrom = new CacheBuilder()
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH)
                .build(this::getSelfPaymentsFromChannelWithoutCache);
        cacheTo = new CacheBuilder()
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH)
                .build(this::getSelfPaymentsToChannelWithoutCache);
        cacheFromClosed = new CacheBuilder()
                .withExpiry(EXPIRY_CLOSED)
                .withRefresh(REFRESH_CLOSED)
                .build(this::getSelfPaymentsFromChannelWithoutCache);
        cacheToClosed = new CacheBuilder()
                .withExpiry(EXPIRY_CLOSED)
                .withRefresh(REFRESH_CLOSED)
                .build(this::getSelfPaymentsToChannelWithoutCache);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId) {
        if (channelService.isClosed(channelId)) {
            return cacheFromClosed.get(channelId);
        }
        return cacheFrom.get(channelId);
    }

    public List<SelfPayment> getSelfPaymentsFromPeer(Pubkey pubkey) {
        return getSelfPaymentsForAllChannels(pubkey, this::getSelfPaymentsFromChannel);
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId) {
        if (channelService.isClosed(channelId)) {
            return cacheToClosed.get(channelId);
        }
        return cacheTo.get(channelId);
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

    private List<SelfPayment> getSelfPaymentsFromChannelWithoutCache(ChannelId channelId) {
        return dao.getSelfPaymentsFromChannel(channelId).stream().distinct().toList();
    }

    private List<SelfPayment> getSelfPaymentsToChannelWithoutCache(ChannelId channelId) {
        return dao.getSelfPaymentsToChannel(channelId);
    }
}
