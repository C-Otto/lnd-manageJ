package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.ChannelIdAndMaxAge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClosedChannelAwareCacheTest {
    private static final ChannelIdAndMaxAge KEY_SHORT = new ChannelIdAndMaxAge(CHANNEL_ID, Duration.ofSeconds(1));
    private static final ChannelIdAndMaxAge KEY_LONG = new ChannelIdAndMaxAge(CHANNEL_ID, Duration.ofDays(1));
    @Mock
    private ChannelService channelService;

    @Mock
    private OwnNodeService ownNodeService;

    @Test
    void withoutExpiry() {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache
                .builder(channelService, ownNodeService)
                .build(0L, this::getNanoTime);
        assertIsCached(cache, KEY_SHORT);
    }

    @Test
    void expiryOneMillisecond() throws InterruptedException {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .withExpiry(Duration.ofMillis(1))
                .build(0L, this::getNanoTime);
        Long first = cache.get(KEY_SHORT);
        Thread.sleep(1);
        Long second = cache.get(KEY_SHORT);
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void for_open_channel() {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .build(0L, this::getNanoTime);
        when(channelService.getClosedChannel(KEY_LONG.channelId())).thenReturn(Optional.empty());
        assertIsCached(cache, KEY_LONG);
    }

    @Test
    void for_recently_closed_channel() {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .build(0L, this::getNanoTime);
        when(channelService.getClosedChannel(KEY_LONG.channelId())).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(500 + CLOSED_CHANNEL.getCloseHeight());
        assertIsCached(cache, KEY_LONG);
    }

    @Test
    void for_recently_closed_channel_and_very_short_max_age() {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .build(0L, this::getNanoTime);
        when(channelService.getClosedChannel(KEY_SHORT.channelId())).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(ownNodeService.getBlockHeight()).thenReturn(400 + CLOSED_CHANNEL.getCloseHeight());
        Long first = cache.get(KEY_SHORT);
        Long second = cache.get(KEY_SHORT);
        assertThat(first).isEqualTo(second).isZero();
    }

    @Test
    void for_closed_channel_closed_long_ago() {
        ClosedChannelAwareCache<Long> cache = ClosedChannelAwareCache.builder(channelService, ownNodeService)
                .build(0L, this::getNanoTime);
        when(channelService.getClosedChannel(KEY_LONG.channelId())).thenReturn(Optional.of(CLOSED_CHANNEL_2));
        when(ownNodeService.getBlockHeight()).thenReturn(1_000 + CLOSED_CHANNEL_2.getCloseHeight());
        Long first = cache.get(KEY_LONG);
        Long second = cache.get(KEY_LONG);
        assertThat(first).isEqualTo(second).isZero();
    }

    private void assertIsCached(ClosedChannelAwareCache<Long> cache, ChannelIdAndMaxAge key) {
        Long first = cache.get(key);
        Long second = cache.get(key);
        assertThat(first).isEqualTo(second).isNotZero();
    }

    private Long getNanoTime(@SuppressWarnings("unused") ChannelIdAndMaxAge channelIdAndMaxAge) {
        return System.nanoTime();
    }
}