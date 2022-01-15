package de.cotto.lndmanagej.caching;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.function.Supplier;

public class CacheBuilder extends CacheBuilderBase<CacheBuilder> {

    public CacheBuilder() {
        super();
    }

    @Override
    protected CacheBuilder getThis() {
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
        if (softValues) {
            builder.softValues();
        }
        return builder.build(function);
    }

    public <O> LoadingCache<Object, O> build(Supplier<O> function) {
        return build(ignored -> function.get());
    }
}
