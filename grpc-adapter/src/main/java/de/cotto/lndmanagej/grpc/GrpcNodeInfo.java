package de.cotto.lndmanagej.grpc;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

@Component
public class GrpcNodeInfo {
    private static final int MAXIMUM_SIZE = 500;
    private static final int CACHE_EXPIRY_MINUTES = 30;

    private final GrpcService grpcService;
    private final LoadingCache<Pubkey, Node> cache;

    public GrpcNodeInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
        CacheLoader<Pubkey, Node> loader = new CacheLoader<>() {
            @Override
            public Node load(@Nonnull Pubkey pubkey) {
                return getNodeWithoutCache(pubkey);
            }
        };
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRY_MINUTES, TimeUnit.MINUTES)
                .maximumSize(MAXIMUM_SIZE)
                .build(loader);
    }

    public Node getNode(Pubkey pubkey) {
        return cache.getUnchecked(pubkey);
    }

    private Node getNodeWithoutCache(Pubkey pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey).orElse(null);
        if (nodeInfo == null) {
            return Node.builder().withPubkey(pubkey).build();
        }
        LightningNode node = nodeInfo.getNode();
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(node.getAlias())
                .withLastUpdate(node.getLastUpdate())
                .build();
    }
}
