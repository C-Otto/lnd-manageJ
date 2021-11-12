package de.cotto.lndmanagej.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

@Component
public class OwnNodeService {
    private static final int CACHE_EXPIRY_SECONDS = 30;

    private final GrpcGetInfo grpcGetInfo;
    private final LoadingCache<String, Boolean> syncedToChainCache;

    public OwnNodeService(GrpcGetInfo grpcGetInfo) {
        this.grpcGetInfo = grpcGetInfo;
        CacheLoader<String, Boolean> loader = new CacheLoader<>() {
            @Nonnull
            @Override
            public Boolean load(@Nullable String ignored) {
                return isSyncedToChainWithoutCache();
            }
        };
        syncedToChainCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_SECONDS, TimeUnit.SECONDS)
                .build(loader);
    }

    public boolean isSyncedToChain() {
        return syncedToChainCache.getUnchecked("");
    }

    private boolean isSyncedToChainWithoutCache() {
        return grpcGetInfo.isSyncedToChain().orElse(false);
    }
}
