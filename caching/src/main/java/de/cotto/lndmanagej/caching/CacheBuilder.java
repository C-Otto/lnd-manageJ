package de.cotto.lndmanagej.caching;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Supplier;

public class CacheBuilder {
    private Duration expiry;

    @Nullable
    private Integer maximumSize;

    @Nullable
    private Duration refresh;

    public CacheBuilder() {
        expiry = Duration.ofMinutes(10);
    }

    public CacheBuilder withExpiry(Duration expiry) {
        this.expiry = expiry;
        return this;
    }

    public CacheBuilder withRefresh(Duration refresh) {
        this.refresh = refresh;
        return this;
    }

    public CacheBuilder withMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        return this;
    }

    public <I, O> LoadingCache<I, O> build(CacheLoader<I, O> function) {
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .expireAfterWrite(expiry);
        if (this.refresh != null) {
            builder.refreshAfterWrite(refresh);
        }
        if (this.maximumSize != null) {
            builder.maximumSize(maximumSize);
        }
        return builder.build(function);
    }

    public <O> LoadingCache<Object, O> build(Supplier<O> function) {
        return build(ignored -> function.get());
    }
}
