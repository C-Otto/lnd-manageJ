package de.cotto.lndmanagej.grpc;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import lnrpc.ChannelEdge;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class GrpcChannelPolicy {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;
    private final LoadingCache<ChannelId, Optional<ChannelEdge>> channelEdgeCache;

    public GrpcChannelPolicy(GrpcService grpcService, GrpcGetInfo grpcGetInfo) {
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
        channelEdgeCache = new CacheBuilder()
                .withExpiry(Duration.ofMinutes(1))
                .build(this::getChannelEdgeWithoutCache);
    }

    public Optional<RoutingPolicy> getLocalPolicy(ChannelId channelId) {
        String ownPubkey = grpcGetInfo.getPubkey().toString();
        return getChannelEdge(channelId).map(
                channelEdge -> {
                    if (ownPubkey.equals(channelEdge.getNode1Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (ownPubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode2Policy();
                    } else {
                        return null;
                    }
                }
        );
    }

    public Optional<RoutingPolicy> getRemotePolicy(ChannelId channelId) {
        String ownPubkey = grpcGetInfo.getPubkey().toString();
        return getChannelEdge(channelId).map(
                channelEdge -> {
                    if (ownPubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (ownPubkey.equals(channelEdge.getNode1Pub())) {
                        return channelEdge.getNode2Policy();
                    } else {
                        return null;
                    }
                }
        );
    }

    private Optional<ChannelEdge> getChannelEdge(ChannelId channelId) {
        return channelEdgeCache.get(channelId);
    }

    private Optional<ChannelEdge> getChannelEdgeWithoutCache(ChannelId channelId) {
        return grpcService.getChannelEdge(channelId);
    }

}
