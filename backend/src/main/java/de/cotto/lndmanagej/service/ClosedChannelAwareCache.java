package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.caching.CacheBuilderBase;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdAndMaxAge;
import de.cotto.lndmanagej.model.ClosedChannel;

import java.time.Duration;

public class ClosedChannelAwareCache<O> {
    private final O defaultValue;
    private final ChannelService channelService;
    private final LoadingCache<ChannelIdAndMaxAge, O> cacheOpen;
    private final LoadingCache<ChannelIdAndMaxAge, O> cacheClosed;
    private final OwnNodeService ownNodeService;

    public ClosedChannelAwareCache(
            O defaultValue,
            ChannelService channelService,
            OwnNodeService ownNodeService,
            LoadingCache<ChannelIdAndMaxAge, O> cacheOpen,
            LoadingCache<ChannelIdAndMaxAge, O> cacheClosed
    ) {
        this.channelService = channelService;
        this.cacheOpen = cacheOpen;
        this.cacheClosed = cacheClosed;
        this.defaultValue = defaultValue;
        this.ownNodeService = ownNodeService;
    }

    public static Builder builder(ChannelService channelService, OwnNodeService ownNodeService) {
        return new Builder(channelService, ownNodeService);
    }

    public O get(ChannelId channelId, Duration maxAge) {
        return get(new ChannelIdAndMaxAge(channelId, maxAge));
    }

    public O get(ChannelIdAndMaxAge channelIdAndMaxAge) {
        ChannelId channelId = channelIdAndMaxAge.channelId();
        ClosedChannel closedChannel = channelService.getClosedChannel(channelId).orElse(null);
        if (closedChannel == null) {
            return cacheOpen.get(channelIdAndMaxAge);
        }
        Duration maxAge = channelIdAndMaxAge.maxAge();
        if (isClosedLongerThan(closedChannel, maxAge)) {
            return defaultValue;
        }
        return cacheClosed.get(channelIdAndMaxAge);
    }

    private boolean isClosedLongerThan(ClosedChannel closedChannel, Duration maxAge) {
        int blocksSinceClose = ownNodeService.getBlockHeight() - closedChannel.getCloseHeight();
        int daysClosedWithSafetyMargin = (int) (0.5 * blocksSinceClose * 10.0 / (60 * 24));
        return maxAge.minus(Duration.ofDays(daysClosedWithSafetyMargin)).isNegative();
    }

    public static class Builder extends CacheBuilderBase<Builder> {
        private static final Duration REFRESH_CLOSED = Duration.ofHours(12);
        private static final Duration EXPIRY_CLOSED = Duration.ofHours(24);
        private final ChannelService channelService;
        private final OwnNodeService ownNodeService;

        public Builder(ChannelService channelService, OwnNodeService ownNodeService) {
            super();
            this.channelService = channelService;
            this.ownNodeService = ownNodeService;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public <O> ClosedChannelAwareCache<O> build(O defaultValue, CacheLoader<ChannelIdAndMaxAge, O> function) {
            CacheBuilder builder = new CacheBuilder()
                    .withRefresh(refresh)
                    .withExpiry(expiry)
                    .withSoftValues(softValues)
                    .withMaximumSize(maximumSize);
            LoadingCache<ChannelIdAndMaxAge, O> cacheOpen = builder.build(function);
            LoadingCache<ChannelIdAndMaxAge, O> cacheClosed = builder
                    .withRefresh(REFRESH_CLOSED)
                    .withExpiry(EXPIRY_CLOSED).build(function);
            return new ClosedChannelAwareCache<>(
                    defaultValue,
                    channelService,
                    ownNodeService,
                    cacheOpen,
                    cacheClosed
            );
        }
    }
}
