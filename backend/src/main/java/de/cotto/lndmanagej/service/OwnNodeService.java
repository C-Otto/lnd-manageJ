package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

@Component
public class OwnNodeService {
    private static final Duration CACHE_REFRESH = Duration.ofSeconds(15);
    private static final Duration CACHE_EXPIRY = Duration.ofSeconds(30);

    private final GrpcGetInfo grpcGetInfo;
    private final LoadingCache<Object, Boolean> syncedToChainCache = new CacheBuilder()
            .withRefresh(CACHE_REFRESH)
            .withExpiry(CACHE_EXPIRY)
            .build(this::isSyncedToChainWithoutCache);

    private final LoadingCache<Object, Integer> blockHeightCache = new CacheBuilder()
            .withRefresh(CACHE_REFRESH)
            .withExpiry(CACHE_EXPIRY)
            .build(this::getBlockHeightWithoutCache);

    public OwnNodeService(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
    }

    public boolean isSyncedToChain() {
        return Boolean.TRUE.equals(syncedToChainCache.get(""));
    }

    public int getBlockHeight() {
        return Objects.requireNonNull(blockHeightCache.get(""));
    }

    private int getBlockHeightWithoutCache() {
        return grpcGetInfo.getBlockHeight().orElseThrow();
    }

    private boolean isSyncedToChainWithoutCache() {
        return grpcGetInfo.isSyncedToChain().orElse(false);
    }
}
