package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdAndMaxAge;
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

    private final SelfPaymentsDao dao;
    private final ChannelService channelService;
    private final ClosedChannelAwareCache<List<SelfPayment>> cacheFrom;
    private final ClosedChannelAwareCache<List<SelfPayment>> cacheTo;

    public SelfPaymentsService(SelfPaymentsDao dao, ChannelService channelService, OwnNodeService ownNodeService) {
        this.dao = dao;
        this.channelService = channelService;
        ClosedChannelAwareCache.Builder builder = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .withSoftValues(true)
                .withExpiry(EXPIRY)
                .withRefresh(REFRESH);
        cacheFrom = builder.build(List.of(), this::getSelfPaymentsFromChannelWithoutCache);
        cacheTo = builder.build(List.of(), this::getSelfPaymentsToChannelWithoutCache);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId) {
        return getSelfPaymentsFromChannel(channelId, DEFAULT_MAX_AGE);
    }

    public List<SelfPayment> getSelfPaymentsFromChannel(ChannelId channelId, Duration maxAge) {
        return cacheFrom.get(channelId, maxAge);
    }

    public List<SelfPayment> getSelfPaymentsFromPeer(Pubkey pubkey) {
        return getSelfPaymentsForAllChannels(pubkey, this::getSelfPaymentsFromChannel);
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId) {
        return getSelfPaymentsToChannel(channelId, DEFAULT_MAX_AGE);
    }

    public List<SelfPayment> getSelfPaymentsToChannel(ChannelId channelId, Duration maxAge) {
        return cacheTo.get(channelId, maxAge);
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
        ChannelId channelId = channelIdAndMaxAge.channelId();
        return dao.getSelfPaymentsFromChannel(channelId, channelIdAndMaxAge.maxAge()).stream()
                .filter(selfPayment -> noLowerChannelIdAsFirstHop(channelId, selfPayment))
                .distinct()
                .toList();
    }

    private List<SelfPayment> getSelfPaymentsToChannelWithoutCache(ChannelIdAndMaxAge channelIdAndMaxAge) {
        ChannelId channelId = channelIdAndMaxAge.channelId();
        return dao.getSelfPaymentsToChannel(channelId, channelIdAndMaxAge.maxAge()).stream()
                .filter(selfPayment -> noLowerChannelIdAsLastHop(channelId, selfPayment))
                .distinct()
                .toList();
    }

    private boolean noLowerChannelIdAsFirstHop(ChannelId channelId, SelfPayment selfPayment) {
        return selfPayment.routes().stream().allMatch(route -> channelId.compareTo(route.channelIdOut()) <= 0);
    }

    private boolean noLowerChannelIdAsLastHop(ChannelId channelId, SelfPayment selfPayment) {
        return selfPayment.routes().stream().allMatch(route -> channelId.compareTo(route.channelIdIn()) <= 0);
    }
}
