package de.cotto.lndmanagej.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

@Component
public class NodeService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 30;

    private final GrpcNodeInfo grpcNodeInfo;
    private final LoadingCache<Pubkey, String> aliasCache;

    public NodeService(GrpcNodeInfo grpcNodeInfo) {
        this.grpcNodeInfo = grpcNodeInfo;
        CacheLoader<Pubkey, String> loader = new CacheLoader<>() {
            @Nonnull
            @Override
            public String load(@Nonnull Pubkey pubkey) {
                return getNode(pubkey).alias();
            }
        };
        aliasCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(loader);
    }

    public String getAlias(Pubkey pubkey) {
        return aliasCache.getUnchecked(pubkey);
    }

    private Node getNode(Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey);
    }
}
