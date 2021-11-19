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
    private static final int ALIAS_CACHE_EXPIRY_MINUTES = 30;
    private static final int NODE_CACHE_EXPIRY_SECONDS = 60;

    private final GrpcNodeInfo grpcNodeInfo;
    private final LoadingCache<Pubkey, String> aliasCache = new CacheBuilder()
            .withExpiryMinutes(ALIAS_CACHE_EXPIRY_MINUTES)
            .withMaximumSize(MAXIMUM_SIZE)
            .build(this::getAliasWithoutCache);
    private final LoadingCache<Pubkey, Node> nodeCache = new CacheBuilder()
            .withExpirySeconds(NODE_CACHE_EXPIRY_SECONDS)
            .withMaximumSize(MAXIMUM_SIZE)
            .build(this::getNodeWithoutCache);

    public NodeService(GrpcNodeInfo grpcNodeInfo) {
        this.grpcNodeInfo = grpcNodeInfo;
    }

    public String getAlias(Pubkey pubkey) {
        return aliasCache.getUnchecked(pubkey);
    }

    public Node getNode(Pubkey pubkey) {
        return nodeCache.getUnchecked(pubkey);
    }

    private Node getNodeWithoutCache(Pubkey pubkey) {
        Node node = grpcNodeInfo.getNode(pubkey);
        aliasCache.put(pubkey, node.alias());
        return node;
    }

    private String getAliasWithoutCache(Pubkey pubkey) {
        return getNodeWithoutCache(pubkey).alias();
    }

}
