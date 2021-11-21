package de.cotto.lndmanagej.caching;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheBuilder {
    private Duration duration;

    @Nullable
    private Integer maximumSize;

    public CacheBuilder() {
        duration = Duration.ofMinutes(10);
    }

    public CacheBuilder withExpiry(Duration duration) {
        this.duration = duration;
        return this;
    }

    public CacheBuilder withMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        return this;
    }

    public <I, O> LoadingCache<I, O> build(Function<I, O> function) {
        CacheLoader<I, O> loader = getLoader(function);
        com.google.common.cache.CacheBuilder<Object, Object> builder = com.google.common.cache.CacheBuilder.newBuilder()
                .expireAfterWrite(duration);
        if (this.maximumSize != null) {
            return builder
                    .maximumSize(maximumSize)
                    .build(loader);
        }
        return builder
                .build(loader);
    }

    public <O> LoadingCache<Object, O> build(Supplier<O> function) {
        return build(ignored -> function.get());
    }

    private <I, O> CacheLoader<I, O> getLoader(Function<I, O> function) {
        return new CacheLoader<>() {
            @Nonnull
            @Override
            public O load(@Nonnull I input) {
                return function.apply(input);
            }
        };
    }
}
