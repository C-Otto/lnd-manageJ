package de.cotto.lndmanagej.service;

import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

@Component
public class NodeService {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 30;

    private final GrpcNodeInfo grpcNodeInfo;
    private final LoadingCache<Pubkey, String> aliasCache = new CacheBuilder()
            .withExpiryMinutes(CACHE_EXPIRY_MINUTES)
            .withMaximumSize(MAXIMUM_SIZE)
            .build(this::getAliasWithoutCache);

    public NodeService(GrpcNodeInfo grpcNodeInfo) {
        this.grpcNodeInfo = grpcNodeInfo;
    }

    public String getAlias(Pubkey pubkey) {
        return aliasCache.getUnchecked(pubkey);
    }

    public Node getNode(Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey);
    }

    private String getAliasWithoutCache(Pubkey pubkey) {
        return getNode(pubkey).alias();
    }

}
