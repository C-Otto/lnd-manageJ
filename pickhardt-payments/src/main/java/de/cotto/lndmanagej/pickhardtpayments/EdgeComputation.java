package de.cotto.lndmanagej.pickhardtpayments;

import com.google.common.collect.Sets;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.grpc.middleware.GrpcMiddlewareService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.LiquidityBoundsService;
import de.cotto.lndmanagej.service.RouteHintService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Component
public class EdgeComputation {
    private static final Coins REMOTE_BALANCE_REQUIRED_TO_SEND = Coins.ofSatoshis(400);
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final GrpcGraph grpcGraph;
    private final GrpcGetInfo grpcGetInfo;
    private final ChannelService channelService;
    private final BalanceService balanceService;
    private final LiquidityBoundsService liquidityBoundsService;
    private final RouteHintService routeHintService;
    private final GrpcMiddlewareService grpcMiddlewareService;

    public EdgeComputation(
            GrpcGraph grpcGraph,
            GrpcGetInfo grpcGetInfo,
            ChannelService channelService,
            BalanceService balanceService,
            LiquidityBoundsService liquidityBoundsService,
            RouteHintService routeHintService,
            GrpcMiddlewareService grpcMiddlewareService
    ) {
        this.grpcGraph = grpcGraph;
        this.grpcGetInfo = grpcGetInfo;
        this.channelService = channelService;
        this.balanceService = balanceService;
        this.liquidityBoundsService = liquidityBoundsService;
        this.routeHintService = routeHintService;
        this.grpcMiddlewareService = grpcMiddlewareService;
    }

    public EdgesWithLiquidityInformation getEdges(PaymentOptions paymentOptions, int maximumTimeLockDeltaPerEdge) {
        if (noMiddlewareSupport()) {
            logger.error("Middleware needs to be connected, see requirements section in PickhardtPayments.md");
            return EdgesWithLiquidityInformation.EMPTY;
        }
        Set<DirectedChannelEdge> channelEdges = grpcGraph.getChannelEdges().orElse(null);
        if (channelEdges == null) {
            logger.error("Unable to get graph");
            return EdgesWithLiquidityInformation.EMPTY;
        }
        Set<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = new LinkedHashSet<>();
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        Set<DirectedChannelEdge> edgesFromPaymentHints = routeHintService.getEdgesFromPaymentHints();
        for (DirectedChannelEdge channelEdge : Sets.union(channelEdges, edgesFromPaymentHints)) {
            if (shouldIgnore(channelEdge, paymentOptions, ownPubkey, maximumTimeLockDeltaPerEdge)) {
                continue;
            }
            ChannelId channelId = channelEdge.channelId();
            Pubkey pubkey1 = channelEdge.source();
            Pubkey pubkey2 = channelEdge.target();
            Edge edge = new Edge(channelId, pubkey1, pubkey2, channelEdge.capacity(), channelEdge.policy());
            if (edgesFromPaymentHints.contains(channelEdge)) {
                edgesWithLiquidityInformation.add(
                        EdgeWithLiquidityInformation.forLowerAndUpperBound(edge, edge.capacity(), edge.capacity())
                );
            } else {
                edgesWithLiquidityInformation.add(getEdgeWithLiquidityInformation(edge, ownPubkey));
            }
        }
        logger.debug("Edges with liquidity information: {}", edgesWithLiquidityInformation);
        return new EdgesWithLiquidityInformation(edgesWithLiquidityInformation);
    }

    public EdgeWithLiquidityInformation getEdgeWithLiquidityInformation(Edge edge) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return getEdgeWithLiquidityInformation(edge, ownPubkey);
    }

    private EdgeWithLiquidityInformation getEdgeWithLiquidityInformation(Edge edge, Pubkey ownPubkey) {
        Coins knownLiquidity = getKnownLiquidity(edge, ownPubkey).orElse(null);
        if (knownLiquidity != null) {
            return EdgeWithLiquidityInformation.forKnownLiquidity(edge, knownLiquidity);
        }
        Coins lowerBound = liquidityBoundsService.getAssumedLiquidityLowerBound(edge);
        Coins upperBound = getAvailableLiquidityUpperBound(edge, lowerBound);
        return EdgeWithLiquidityInformation.forLowerAndUpperBound(edge, lowerBound, upperBound);
    }

    private boolean shouldIgnore(
            DirectedChannelEdge channelEdge,
            PaymentOptions paymentOptions,
            Pubkey pubkey,
            int maximumTimeLockDeltaPerEdge
    ) {
        Policy policy = channelEdge.policy();
        if (policy.disabled()) {
            return true;
        }
        if (policy.timeLockDelta() > maximumTimeLockDeltaPerEdge) {
            return true;
        }
        Long feeRateLimit = paymentOptions.feeRateLimit().orElse(null);
        if (feeRateLimit == null) {
            return false;
        }
        long feeRate = policy.feeRate();
        if (feeRate >= feeRateLimit) {
            return true;
        }
        if (isIncomingEdge(channelEdge, pubkey)) {
            return false;
        }
        Long feeRateLimitFirstHops = paymentOptions.feeRateLimitExceptIncomingHops().orElse(null);
        if (feeRateLimitFirstHops == null) {
            return false;
        }
        return feeRate >= feeRateLimitFirstHops;
    }

    private boolean isIncomingEdge(DirectedChannelEdge channelEdge, Pubkey ownPubkey) {
        return ownPubkey.equals(channelEdge.target());
    }

    private Optional<Coins> getKnownLiquidity(Edge edge, Pubkey ownPubKey) {
        Pubkey source = edge.startNode();
        ChannelId channelId = edge.channelId();
        if (ownPubKey.equals(source)) {
            Coins availableRemote = getLocalChannelAvailableRemote(channelId).orElse(Coins.NONE);
            if (availableRemote.subtract(REMOTE_BALANCE_REQUIRED_TO_SEND).isNonPositive()) {
                return Optional.of(Coins.NONE);
            }
            return getLocalChannelAvailableLocal(channelId);
        }
        Pubkey target = edge.endNode();
        if (ownPubKey.equals(target)) {
            return getLocalChannelAvailableRemote(channelId);
        }
        return Optional.empty();
    }

    private Optional<Coins> getLocalChannelAvailableLocal(ChannelId channelId) {
        return getLocalChannelAvailable(channelId, balanceService::getAvailableLocalBalance);
    }

    private Optional<Coins> getLocalChannelAvailableRemote(ChannelId channelId) {
        return getLocalChannelAvailable(channelId, balanceService::getAvailableRemoteBalance);
    }

    private Optional<Coins> getLocalChannelAvailable(ChannelId channelId, Function<ChannelId, Coins> balanceProvider) {
        LocalOpenChannel localChannel = channelService.getOpenChannel(channelId).orElse(null);
        if (localChannel == null) {
            return Optional.of(Coins.NONE);
        }
        if (localChannel.getStatus().active()) {
            return Optional.of(balanceProvider.apply(channelId));
        }
        return Optional.of(Coins.NONE);
    }

    private Coins getAvailableLiquidityUpperBound(Edge edge, Coins lowerBound) {
        Coins upperBound = liquidityBoundsService.getAssumedLiquidityUpperBound(edge).orElse(null);
        return edge.capacity().minimum(upperBound).maximum(lowerBound);
    }

    private boolean noMiddlewareSupport() {
        return !grpcMiddlewareService.isConnected();
    }
}
