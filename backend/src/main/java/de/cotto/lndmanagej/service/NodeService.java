package de.cotto.lndmanagej.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NodeService {
    private static final int MAXIMUM_SIZE = 500;
    private static final Duration ALIAS_CACHE_EXPIRY = Duration.ofHours(24);
    private static final Duration ALIAS_CACHE_REFRESH = Duration.ofMinutes(30);
    private static final Duration NODE_CACHE_EXPIRY = Duration.ofSeconds(60);

    private final GrpcNodeInfo grpcNodeInfo;
    private final LoadingCache<Pubkey, String> aliasCache = new CacheBuilder()
            .withExpiry(ALIAS_CACHE_EXPIRY)
            .withRefresh(ALIAS_CACHE_REFRESH)
            .withMaximumSize(MAXIMUM_SIZE)
            .build(this::getAliasWithoutCache);
    private final LoadingCache<Pubkey, Node> nodeCache = new CacheBuilder()
            .withExpiry(NODE_CACHE_EXPIRY)
            .withMaximumSize(MAXIMUM_SIZE)
            .build(this::getNodeWithoutCacheAndUpdateAliasCache);

    public NodeService(GrpcNodeInfo grpcNodeInfo) {
        this.grpcNodeInfo = grpcNodeInfo;
    }

    public String getAlias(Pubkey pubkey) {
        return aliasCache.get(pubkey);
    }

    public Node getNode(Pubkey pubkey) {
        return nodeCache.get(pubkey);
    }

    private Node getNodeWithoutCacheAndUpdateAliasCache(Pubkey pubkey) {
        Node node = grpcNodeInfo.getNodeWithOnlineStatus(pubkey);
        aliasCache.put(pubkey, node.alias());
        return node;
    }

    private String getAliasWithoutCache(Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey).alias();
    }

}
