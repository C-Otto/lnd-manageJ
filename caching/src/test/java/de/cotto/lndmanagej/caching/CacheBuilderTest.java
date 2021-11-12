package de.cotto.lndmanagej.caching;

import com.google.common.cache.LoadingCache;
import org.junit.jupiter.api.Test;

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
                .withExpiryMilliseconds(1)
                .build(System::nanoTime);
        Long first = cache.getUnchecked("");
        Thread.sleep(1);
        Long second = cache.getUnchecked("");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void expiryOneSecond() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpirySeconds(1)
                .build(System::nanoTime);
        assertIsCached(cache);
    }

    @Test
    void expiryOneMinute() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpiryMinutes(1)
                .build(System::nanoTime);
        assertIsCached(cache);
    }

    @Test
    void withMaximumSize() {
        LoadingCache<Object, Long> cache = new CacheBuilder()
                .withExpiryMinutes(1)
                .withMaximumSize(1)
                .build(System::nanoTime);
        Long first = cache.getUnchecked("");
        cache.getUnchecked("a");
        Long third = cache.getUnchecked("");
        assertThat(first).isNotEqualTo(third);
    }

    private void assertIsCached(LoadingCache<Object, Long> cache) {
        Long first = cache.getUnchecked("");
        Long second = cache.getUnchecked("");
        assertThat(first).isEqualTo(second);
    }
}