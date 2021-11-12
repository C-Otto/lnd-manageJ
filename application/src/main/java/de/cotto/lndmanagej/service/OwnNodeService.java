package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.springframework.stereotype.Component;

@Component
public class OwnNodeService {
    private static final int CACHE_EXPIRY_SECONDS = 30;

    private final GrpcGetInfo grpcGetInfo;
    private final LoadingCache<Object, Boolean> syncedToChainCache = new CacheBuilder()
            .withExpirySeconds(CACHE_EXPIRY_SECONDS)
            .build(this::isSyncedToChainWithoutCache);

    public OwnNodeService(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
    }

    public boolean isSyncedToChain() {
        return syncedToChainCache.getUnchecked("");
    }

    private boolean isSyncedToChainWithoutCache() {
        return grpcGetInfo.isSyncedToChain().orElse(false);
    }
}
