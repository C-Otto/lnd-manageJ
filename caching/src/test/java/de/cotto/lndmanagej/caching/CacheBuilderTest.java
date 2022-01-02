package de.cotto.lndmanagej.caching;

import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CacheBuilderTest {

    @Test
    void withoutExpiry() {
        LoadingCache<Object, Long> cache = new CacheBuilder().build(System::nanoTime);
        assertIsCached(cache);
    }

    @Test
    void expiryOneMillisecond() throws InterruptedException {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpiry(Duration.ofMillis(1))
                .build(System::nanoTime);
        Long first = cache.get("");
        Thread.sleep(1);
        Long second = cache.get("");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void withRefresh_cached_value() throws InterruptedException {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withRefresh(Duration.ofMillis(10))
                .build(System::nanoTime);
        Long first = cache.get("");
        Thread.sleep(1);
        Long second = cache.get("");
        assertThat(first).isEqualTo(second);
    }

    @Test
    void withRefresh_cached_value_async_refresh() throws InterruptedException {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withRefresh(Duration.ofMillis(1))
                .build(() -> {
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        // ignored
                    }
                    return System.nanoTime();
                });
        Long first = cache.get("");
        Thread.sleep(1);
        Long second = cache.get("");
        Thread.sleep(5);
        Long third = cache.get("");
        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(third);
    }

    @Test
    void withRefresh_returns_old_value() throws InterruptedException {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withRefresh(Duration.ofMillis(1))
                .build(System::nanoTime);
        Long first = cache.get("");
        Thread.sleep(10);
        Long second = cache.get("");
        assertThat(first).isEqualTo(second);
    }

    @Test
    @SuppressWarnings("FutureReturnValueIgnored")
    void withRefresh_returns_new_value_after_refresh() throws InterruptedException {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withRefresh(Duration.ofMillis(1))
                .build(System::nanoTime);
        Long first = cache.get("");
        Thread.sleep(50);
        cache.refresh("");
        Thread.sleep(50);
        Long second = cache.get("");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void expiryOneMinute() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpiry(Duration.ofMinutes(1))
                .build(System::nanoTime);
        assertIsCached(cache);
    }

    @Test
    void withMaximumSize() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpiry(Duration.ofMinutes(1))
                .withMaximumSize(1)
                .build(System::nanoTime);
        Long first = cache.get("");
        cache.get("a");
        cache.cleanUp();
        Long third = cache.get("");
        assertThat(first).isNotEqualTo(third);
    }

    @Test
    void withSoftValues() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withSoftValues(true)
                .build(System::nanoTime);
        assertThat(cache.get("")).isNotNull();
    }

    private void assertIsCached(LoadingCache<Object, Long> cache) {
        Long first = cache.get("");
        Long second = cache.get("");
        assertThat(first).isEqualTo(second);
    }
}