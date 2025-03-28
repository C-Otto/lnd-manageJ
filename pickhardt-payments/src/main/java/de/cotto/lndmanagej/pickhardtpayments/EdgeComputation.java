package de.cotto.lndmanagej.pickhardtpayments;

import com.google.common.collect.Sets;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.grpc.middleware.GrpcMiddlewareService;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
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
    private static final Coins REMOTE_BALANCE_REQUIRED_TO_SEND = Coins.ofSatoshis(4223);
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
        Set<Edge> channelEdges = grpcGraph.getChannelEdges().orElse(null);
        if (channelEdges == null) {
            logger.error("Unable to get graph");
            return EdgesWithLiquidityInformation.EMPTY;
        }
        Set<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = new LinkedHashSet<>();
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        Set<Edge> edgesFromPaymentHints = routeHintService.getEdgesFromPaymentHints();
        for (Edge edge : Sets.union(channelEdges, edgesFromPaymentHints)) {
            if (shouldIgnore(edge, paymentOptions, ownPubkey, maximumTimeLockDeltaPerEdge)) {
                continue;
            }
            if (edgesFromPaymentHints.contains(edge)) {
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

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    private boolean shouldIgnore(
            Edge channelEdge,
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
        boolean hopIsRelevantForFeeCheck =
                !pubkey.equals(channelEdge.startNode()) || !paymentOptions.ignoreFeesForOwnChannels();
        if (feeRate >= feeRateLimit && hopIsRelevantForFeeCheck) {
            return true;
        }
        if (isEdgeToUnwantedFirstHop(channelEdge, paymentOptions, pubkey)) {
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

    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    private boolean isEdgeToUnwantedFirstHop(
            Edge channelEdge,
            PaymentOptions paymentOptions,
            Pubkey pubkey
    ) {
        boolean isOutgoingEdge = pubkey.equals(channelEdge.startNode());
        if (!isOutgoingEdge) {
            return false;
        }
        Pubkey peerForFirstHop = paymentOptions.peerForFirstHop().orElse(null);
        boolean firstHopIsUnexpected = peerForFirstHop != null && !peerForFirstHop.equals(channelEdge.endNode());
        if (firstHopIsUnexpected) {
            return true;
        }
        Pubkey peerForLastHop = paymentOptions.peer().orElse(null);
        if (peerForLastHop == null) {
            return false;
        }
        return peerForLastHop.equals(channelEdge.endNode());
    }

    private boolean isIncomingEdge(Edge channelEdge, Pubkey ownPubkey) {
        return ownPubkey.equals(channelEdge.endNode());
    }

    private Optional<Coins> getKnownLiquidity(Edge edge, Pubkey ownPubKey) {
        Pubkey source = edge.startNode();
        ChannelId channelId = edge.channelId();
        if (ownPubKey.equals(source)) {
            Coins availableRemote = getLocalChannelAvailableRemote(channelId, edge.reversePolicy()).orElse(Coins.NONE);
            if (availableRemote.subtract(REMOTE_BALANCE_REQUIRED_TO_SEND).isNonPositive()) {
                return Optional.of(Coins.NONE);
            }
            return getLocalChannelAvailableLocal(channelId, edge.policy());
        }
        Pubkey target = edge.endNode();
        if (ownPubKey.equals(target)) {
            return getLocalChannelAvailableRemote(channelId, edge.policy());
        }
        return Optional.empty();
    }

    private Optional<Coins> getLocalChannelAvailableLocal(ChannelId channelId, Policy policy) {
        return getLocalChannelAvailable(channelId, balanceService::getAvailableLocalBalance, policy);
    }

    private Optional<Coins> getLocalChannelAvailableRemote(ChannelId channelId, Policy policy) {
        return getLocalChannelAvailable(channelId, balanceService::getAvailableRemoteBalance, policy);
    }

    private Optional<Coins> getLocalChannelAvailable(
            ChannelId channelId,
            Function<ChannelId, Coins> balanceProvider,
            Policy policy
    ) {
        LocalOpenChannel localChannel = channelService.getOpenChannel(channelId).orElse(null);
        if (localChannel == null) {
            return Optional.of(Coins.NONE);
        }
        if (localChannel.getStatus().active()) {
            Coins coinsAvailable = balanceProvider.apply(channelId);
            return Optional.of(coinsAvailable.minimum(policy.maxHtlc()));
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
