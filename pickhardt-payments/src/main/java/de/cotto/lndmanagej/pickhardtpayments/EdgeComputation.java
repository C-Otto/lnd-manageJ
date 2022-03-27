package de.cotto.lndmanagej.pickhardtpayments;

import com.github.benmanes.caffeine.cache.LoadingCache;
import de.cotto.lndmanagej.caching.CacheBuilder;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.MissionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Component
public class EdgeComputation {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrpcGraph grpcGraph;
    private final GrpcGetInfo grpcGetInfo;
    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final MissionControlService missionControlService;
    private final LoadingCache<Object, Set<EdgeWithLiquidityInformation>> cache = new CacheBuilder()
            .withExpiry(Duration.ofSeconds(10))
            .withRefresh(Duration.ofSeconds(5))
            .build(this::getEdgesWithoutCache);

    public EdgeComputation(
            GrpcGraph grpcGraph,
            GrpcGetInfo grpcGetInfo,
            ChannelService channelService,
            BalanceService balanceService,
            MissionControlService missionControlService
    ) {
        this.grpcGraph = grpcGraph;
        this.grpcGetInfo = grpcGetInfo;
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.missionControlService = missionControlService;
    }

    public Set<EdgeWithLiquidityInformation> getEdges() {
        return cache.get("");
    }

    private Set<EdgeWithLiquidityInformation> getEdgesWithoutCache() {
        Set<DirectedChannelEdge> channelEdges = grpcGraph.getChannelEdges().orElse(null);
        if (channelEdges == null) {
            logger.warn("Unable to get graph");
            return Set.of();
        }
        Set<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = new LinkedHashSet<>();
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        for (DirectedChannelEdge channelEdge : channelEdges) {
            if (channelEdge.policy().disabled()) {
                continue;
            }
            ChannelId channelId = channelEdge.channelId();
            Pubkey pubkey1 = channelEdge.source();
            Pubkey pubkey2 = channelEdge.target();
            Edge edge = new Edge(channelId, pubkey1, pubkey2, channelEdge.capacity(), channelEdge.policy());
            edgesWithLiquidityInformation.add(getEdgeWithLiquidityInformation(edge, ownPubkey));
        }
        return edgesWithLiquidityInformation;
    }

    private EdgeWithLiquidityInformation getEdgeWithLiquidityInformation(Edge edge, Pubkey ownPubkey) {
        Coins knownLiquidity = getKnownLiquidity(edge, ownPubkey).orElse(null);
        if (knownLiquidity == null) {
            Coins availableLiquidityUpperBound = getAvailableLiquidityUpperBound(edge);
            return EdgeWithLiquidityInformation.forUpperBound(edge, availableLiquidityUpperBound);
        }
        return EdgeWithLiquidityInformation.forKnownLiquidity(edge, knownLiquidity);
    }

    private Optional<Coins> getKnownLiquidity(Edge edge, Pubkey ownPubKey) {
        Pubkey source = edge.startNode();
        ChannelId channelId = edge.channelId();
        if (ownPubKey.equals(source)) {
            return getLocalChannelAvailableLocal(channelId);
        }
        Pubkey target = edge.endNode();
        if (ownPubKey.equals(target)) {
            return getLocalChannelAvailableRemote(channelId);
        }
        return Optional.empty();
    }

    private Optional<Coins> getLocalChannelAvailableLocal(ChannelId channelId) {
        return channelService.getLocalChannel(channelId)
                .map(c -> balanceService.getAvailableLocalBalance(channelId));
    }

    private Optional<Coins> getLocalChannelAvailableRemote(ChannelId channelId) {
        return channelService.getLocalChannel(channelId)
                .map(c -> balanceService.getAvailableRemoteBalance(channelId));
    }

    private Coins getAvailableLiquidityUpperBound(Edge edge) {
        Pubkey source = edge.startNode();
        Pubkey target = edge.endNode();
        Coins failureAmount = missionControlService.getMinimumOfRecentFailures(source, target).orElse(null);
        if (failureAmount == null) {
            return edge.capacity();
        }
        long satsCapacity = edge.capacity().satoshis();
        long satsNotAvailable = failureAmount.milliSatoshis() / 1_000;
        long satsAvailable = Math.max(Math.min(satsNotAvailable - 1, satsCapacity), 0);
        return Coins.ofSatoshis(satsAvailable);
    }
}
