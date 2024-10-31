package de.cotto.lndmanagej.grpc;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelEdge;
import lnrpc.ChannelGraph;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class GrpcGraph {
    private static final Policy DEFAULT_DISABLED_POLICY = new Policy(0, Coins.NONE, false, 0, Coins.NONE, Coins.NONE);
    private final GrpcService grpcService;
    private final LoadingCache<Object, Optional<Set<Edge>>> channelEdgeCache;
    private final GrpcPolicy grpcPolicy;

    public GrpcGraph(GrpcService grpcService, GrpcPolicy grpcPolicy) {
        this.grpcService = grpcService;
        this.grpcPolicy = grpcPolicy;
        channelEdgeCache = new CacheBuilder()
                .withExpiry(Duration.ofMinutes(2))
                .withRefresh(Duration.ofMinutes(1))
                .withSoftValues(true)
                .build(this::getChannelEdgesWithoutCache);
    }

    public Optional<Set<Edge>> getChannelEdges() {
        return channelEdgeCache.get("");
    }

    public void resetCache() {
        channelEdgeCache.invalidateAll();
    }

    private Optional<Set<Edge>> getChannelEdgesWithoutCache() {
        ChannelGraph channelGraph = grpcService.describeGraph().orElse(null);
        if (channelGraph == null) {
            return Optional.empty();
        }
        Set<Edge> channelEdges = new LinkedHashSet<>();
        for (ChannelEdge channelEdge : channelGraph.getEdgesList()) {
            ChannelId channelId = ChannelId.fromShortChannelId(channelEdge.getChannelId());
            Coins capacity = Coins.ofSatoshis(channelEdge.getCapacity());
            Pubkey node1Pubkey = Pubkey.create(channelEdge.getNode1Pub());
            Pubkey node2Pubkey = Pubkey.create(channelEdge.getNode2Pub());
            Policy node1Policy = getNode1Policy(channelEdge);
            Policy node2Policy = getNode2Policy(channelEdge);
            Edge edge1 = new Edge(
                    channelId,
                    node1Pubkey,
                    node2Pubkey,
                    capacity,
                    node1Policy,
                    node2Policy
            );
            Edge edge2 = new Edge(
                    channelId,
                    node2Pubkey,
                    node1Pubkey,
                    capacity,
                    node2Policy,
                    node1Policy
            );
            channelEdges.add(edge1);
            channelEdges.add(edge2);
        }
        return Optional.of(channelEdges);
    }

    private Policy getNode1Policy(ChannelEdge channelEdge) {
        if (channelEdge.hasNode1Policy()) {
            return grpcPolicy.toPolicy(channelEdge.getNode1Policy());
        }
        return DEFAULT_DISABLED_POLICY;
    }

    private Policy getNode2Policy(ChannelEdge channelEdge) {
        if (channelEdge.hasNode2Policy()) {
            return grpcPolicy.toPolicy(channelEdge.getNode2Policy());
        }
        return DEFAULT_DISABLED_POLICY;
    }
}
