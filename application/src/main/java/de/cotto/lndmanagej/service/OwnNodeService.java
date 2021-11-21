package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class OwnNodeService {
    private static final Duration CACHE_EXPIRY = Duration.ofSeconds(30);

    private final GrpcGetInfo grpcGetInfo;
    private final LoadingCache<Object, Boolean> syncedToChainCache = new CacheBuilder()
            .withExpiry(CACHE_EXPIRY)
            .build(this::isSyncedToChainWithoutCache);

    public OwnNodeService(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
    }

    public boolean isSyncedToChain() {
        return Boolean.TRUE.equals(syncedToChainCache.get(""));
    }

    private boolean isSyncedToChainWithoutCache() {
        return grpcGetInfo.isSyncedToChain().orElse(false);
    }
}
