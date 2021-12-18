package de.cotto.lndmanagej.statistics;

import com.codahale.metrics.annotation.Timed;
import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.PrivateResolver;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class PrivateChannelsResolverImpl implements PrivateResolver {
    private final PrivateChannelsDao dao;
    private final LoadingCache<ChannelId, Boolean> cache;

    public PrivateChannelsResolverImpl(PrivateChannelsDao dao) {
        this.dao = dao;
        cache = new CacheBuilder()
                .withExpiry(Duration.ofHours(1))
                .withRefresh(Duration.ofMinutes(30))
                .build(this::isPrivateWithoutCache);
    }

    @Timed
    @Override
    public boolean isPrivate(ChannelId channelId) {
        return Objects.requireNonNull(cache.get(channelId));
    }

    private boolean isPrivateWithoutCache(ChannelId channelId) {
        return dao.isPrivate(channelId).orElse(false);
    }
}
