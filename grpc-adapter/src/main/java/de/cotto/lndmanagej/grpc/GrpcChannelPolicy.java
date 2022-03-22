package de.cotto.lndmanagej.grpc;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Pubkey;
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
                .withExpiry(Duration.ofSeconds(10))
                .build(this::getChannelEdgeWithoutCache);
    }

    public Optional<RoutingPolicy> getLocalPolicy(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return getPolicyFrom(channelId, ownPubkey);
    }

    public Optional<RoutingPolicy> getRemotePolicy(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return getPolicyTo(channelId, ownPubkey);
    }

    public Optional<RoutingPolicy> getPolicyFrom(ChannelId channelId, Pubkey source) {
        String sourcePubkey = source.toString();
        return getChannelEdge(channelId).map(
                channelEdge -> {
                    if (sourcePubkey.equals(channelEdge.getNode1Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (sourcePubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode2Policy();
                    } else {
                        return null;
                    }
                }
        );
    }

    public Optional<RoutingPolicy> getPolicyTo(ChannelId channelId, Pubkey target) {
        String targetPubkey = target.toString();
        return getChannelEdge(channelId).map(
                channelEdge -> {
                    if (targetPubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (targetPubkey.equals(channelEdge.getNode1Pub())) {
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
