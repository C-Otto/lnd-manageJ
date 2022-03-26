package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.MissionControlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class FlowComputation {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GrpcGraph grpcGraph;
    private final GrpcGetInfo grpcGetInfo;
    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final MissionControlService missionControlService;
    private final long quantization;
    private final int piecewiseLinearApproximations;

    public FlowComputation(
            GrpcGraph grpcGraph,
            GrpcGetInfo grpcGetInfo,
            ChannelService channelService,
            BalanceService balanceService,
            MissionControlService missionControlService,
            @Value("${lndmanagej.pickhardtpayments.quantization:10000}") long quantization,
            @Value("${lndmanagej.pickhardtpayments.piecewiseLinearApproximations:5}") int piecewiseLinearApproximations
    ) {
        this.grpcGraph = grpcGraph;
        this.grpcGetInfo = grpcGetInfo;
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.missionControlService = missionControlService;
        this.quantization = quantization;
        this.piecewiseLinearApproximations = piecewiseLinearApproximations;
    }

    public Flows getOptimalFlows(Pubkey source, Pubkey target, Coins amount) {
        MinCostFlowSolver minCostFlowSolver = new MinCostFlowSolver(
                getEdges(),
                Map.of(source, amount),
                Map.of(target, amount),
                quantization,
                piecewiseLinearApproximations
        );
        return minCostFlowSolver.solve();
    }

    private Set<EdgeWithLiquidityInformation> getEdges() {
        Set<DirectedChannelEdge> channelEdges = grpcGraph.getChannelEdges().orElse(null);
        if (channelEdges == null) {
            logger.warn("Unable to get graph");
            return Set.of();
        }
        Set<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = new LinkedHashSet<>();
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        for (DirectedChannelEdge channelEdge : channelEdges) {
            if (!channelEdge.policy().enabled()) {
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
        Coins capacity = edge.capacity();
        ChannelId channelId = edge.channelId();
        if (ownPubKey.equals(source)) {
            return Optional.of(getLocalChannelAvailableLocal(capacity, channelId));
        }
        Pubkey target = edge.endNode();
        if (ownPubKey.equals(target)) {
            return Optional.of(getLocalChannelAvailableRemote(capacity, channelId));
        }
        return Optional.empty();
    }

    private Coins getLocalChannelAvailableLocal(Coins capacity, ChannelId channelId) {
        return channelService.getLocalChannel(channelId)
                .map(c -> balanceService.getAvailableLocalBalance(channelId))
                .orElse(capacity);
    }

    private Coins getLocalChannelAvailableRemote(Coins capacity, ChannelId channelId) {
        return channelService.getLocalChannel(channelId)
                .map(c -> balanceService.getAvailableRemoteBalance(channelId))
                .orElse(capacity);
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
