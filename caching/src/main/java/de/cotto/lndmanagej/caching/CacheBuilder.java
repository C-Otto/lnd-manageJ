package de.cotto.lndmanagej.caching;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class CacheBuilder {
    private long duration;

    @Nullable
    private TimeUnit timeUnit;

    @Nullable
    private Integer maximumSize;

    public CacheBuilder() {
        duration = 10;
        timeUnit = TimeUnit.MINUTES;
    }

    public CacheBuilder withExpiryMilliseconds(long milliseconds) {
        timeUnit = TimeUnit.MILLISECONDS;
        duration = milliseconds;
        return this;
    }

    public CacheBuilder withExpirySeconds(long seconds) {
        timeUnit = TimeUnit.SECONDS;
        duration = seconds;
        return this;
    }

    public CacheBuilder withExpiryMinutes(long minutes) {
        timeUnit = TimeUnit.MINUTES;
        duration = minutes;
        return this;
    }

    public CacheBuilder withMaximumSize(int maximumSize) {
        this.maximumSize = maximumSize;
        return this;
    }

    public <I, O> LoadingCache<I, O> build(Function<I, O> function) {
        CacheLoader<I, O> loader = getLoader(function);
        com.google.common.cache.CacheBuilder<Object, Object> builder = com.google.common.cache.CacheBuilder.newBuilder()
                .expireAfterWrite(duration, Objects.requireNonNull(timeUnit));
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
