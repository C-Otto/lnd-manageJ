package de.cotto.lndmanagej.grpc;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelEdge;
import lnrpc.ChannelGraph;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class GrpcGraph {
    private final GrpcService grpcService;
    private final LoadingCache<Object, Optional<Set<DirectedChannelEdge>>> channelEdgeCache;

    public GrpcGraph(GrpcService grpcService) {
        this.grpcService = grpcService;
        channelEdgeCache = new CacheBuilder()
                .withExpiry(Duration.ofMinutes(2))
                .withRefresh(Duration.ofMinutes(1))
                .withSoftValues(true)
                .build(this::getChannelEdgesWithoutCache);
    }

    public Optional<Set<DirectedChannelEdge>> getChannelEdges() {
        return channelEdgeCache.get("");
    }

    private Optional<Set<DirectedChannelEdge>> getChannelEdgesWithoutCache() {
        ChannelGraph channelGraph = grpcService.describeGraph().orElse(null);
        if (channelGraph == null) {
            return Optional.empty();
        }
        Set<DirectedChannelEdge> channelEdges = new LinkedHashSet<>();
        for (ChannelEdge channelEdge : channelGraph.getEdgesList()) {
            ChannelId channelId = ChannelId.fromShortChannelId(channelEdge.getChannelId());
            Coins capacity = Coins.ofSatoshis(channelEdge.getCapacity());
            Pubkey node1Pubkey = Pubkey.create(channelEdge.getNode1Pub());
            Pubkey node2Pubkey = Pubkey.create(channelEdge.getNode2Pub());
            DirectedChannelEdge directedChannelEdge1 = new DirectedChannelEdge(
                    channelId,
                    capacity,
                    node1Pubkey,
                    node2Pubkey,
                    toPolicy(channelEdge.getNode1Policy())
            );
            DirectedChannelEdge directedChannelEdge2 = new DirectedChannelEdge(
                    channelId,
                    capacity,
                    node2Pubkey,
                    node1Pubkey,
                    toPolicy(channelEdge.getNode2Policy())
            );
            channelEdges.add(directedChannelEdge1);
            channelEdges.add(directedChannelEdge2);
        }
        return Optional.of(channelEdges);
    }

    private Policy toPolicy(RoutingPolicy routingPolicy) {
        return new Policy(
                routingPolicy.getFeeRateMilliMsat(),
                Coins.ofMilliSatoshis(routingPolicy.getFeeBaseMsat()),
                !routingPolicy.getDisabled(),
                routingPolicy.getTimeLockDelta()
        );
    }
}
