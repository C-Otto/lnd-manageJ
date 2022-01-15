package de.cotto.lndmanagej.caching;

import javax.annotation.Nullable;
import java.time.Duration;

public abstract class CacheBuilderBase<T extends CacheBuilderBase<T>> {
    protected Duration expiry;
    @Nullable
    protected Integer maximumSize;
    @Nullable
    protected Duration refresh;
    protected boolean softValues;

    public CacheBuilderBase() {
        expiry = Duration.ofMinutes(10);
    }

    protected abstract T getThis();

    public T withExpiry(Duration expiry) {
        this.expiry = expiry;
        return getThis();
    }

    public T withRefresh(@Nullable Duration refresh) {
        this.refresh = refresh;
        return getThis();
    }

    public T withMaximumSize(@Nullable Integer maximumSize) {
        this.maximumSize = maximumSize;
        return getThis();
    }

    public T withSoftValues(boolean softValues) {
        this.softValues = softValues;
        return getThis();
    }
}
