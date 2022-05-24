package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.BasicRoute;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.BasicRoutes;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.pickhardtpayments.model.Routes;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MultiPathPaymentSplitter {
    private final GrpcGetInfo grpcGetInfo;
    private final FlowComputation flowComputation;
    private final EdgeComputation edgeComputation;
    private final ChannelService channelService;
    private final PolicyService policyService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MultiPathPaymentSplitter(
            GrpcGetInfo grpcGetInfo,
            FlowComputation flowComputation,
            EdgeComputation edgeComputation,
            ChannelService channelService,
            PolicyService policyService
    ) {
        this.flowComputation = flowComputation;
        this.grpcGetInfo = grpcGetInfo;
        this.edgeComputation = edgeComputation;
        this.channelService = channelService;
        this.policyService = policyService;
    }

    public MultiPathPayment getMultiPathPaymentTo(Pubkey target, Coins amount, PaymentOptions paymentOptions) {
        Pubkey source = grpcGetInfo.getPubkey();
        return getMultiPathPayment(source, target, amount, paymentOptions);
    }

    public MultiPathPayment getMultiPathPayment(
            Pubkey source,
            Pubkey target,
            Coins amount,
            PaymentOptions paymentOptions
    ) {
        Pubkey intermediateTarget = paymentOptions.peer().orElse(target);
        Flows flows = flowComputation.getOptimalFlows(source, intermediateTarget, amount, paymentOptions);
        if (flows.isEmpty()) {
            return MultiPathPayment.FAILURE;
        }
        List<BasicRoute> basicRoutes = BasicRoutes.fromFlows(source, intermediateTarget, flows);
        List<BasicRoute> extendedBasicRoutes = extendBasicRoutes(basicRoutes, paymentOptions, target);
        if (extendedBasicRoutes.isEmpty()) {
            return MultiPathPayment.failure("Unable to extend channel back to own node");
        }
        List<Route> routes = getWithLiquidityInformation(extendedBasicRoutes);
        List<Route> fixedRoutes = Routes.getFixedWithTotalAmount(routes, amount);

        if (isTooExpensive(paymentOptions, fixedRoutes)) {
            return MultiPathPayment.failure("At least one route is too expensive (fee rate limit)");
        }
        return new MultiPathPayment(fixedRoutes);
    }

    private List<BasicRoute> extendBasicRoutes(
            List<BasicRoute> basicRoutes,
            PaymentOptions paymentOptions,
            Pubkey originalTarget
    ) {
        Pubkey peer = paymentOptions.peer().orElse(null);
        if (peer == null) {
            return basicRoutes;
        }
        Set<LocalOpenChannel> channels = channelService.getOpenChannelsWith(peer);
        if (channels.isEmpty()) {
            logger.error("Unable to extend routes for channel with " + peer + " (no channel found)");
            return List.of();
        }
        LocalChannel localChannel = channels.stream().iterator().next();
        ChannelId channelId = localChannel.getId();
        Policy policy = policyService.getPolicyFrom(channelId, peer).orElse(null);
        if (policy == null) {
            logger.error(
                    "Unable to extend routes for channel with " + peer +
                            " (no policy found for channel " + channelId + ")"
            );
            return List.of();
        }
        Coins capacity = localChannel.getCapacity();
        Edge extensionEdge = new Edge(channelId, peer, originalTarget, capacity, policy);
        return basicRoutes.stream().map(basicRoute -> {
            ArrayList<Edge> edges = new ArrayList<>(basicRoute.edges());
            edges.add(extensionEdge);
            return new BasicRoute(edges, basicRoute.amount());
        }).toList();
    }

    private boolean isTooExpensive(PaymentOptions paymentOptions, List<Route> fixedRoutes) {
        if (paymentOptions.feeRateLimit().isEmpty()) {
            return false;
        }

        if (paymentOptions.ignoreFeesForOwnChannels()) {
            long feeRateLimit = paymentOptions.feeRateLimit().get();
            return fixedRoutes.stream().anyMatch(route -> route.getFeeRate() > feeRateLimit);
        }
        long feeRateLimit = paymentOptions.feeRateLimit().get();
        return fixedRoutes.stream().anyMatch(route -> route.getFeeRateWithFirstHop() > feeRateLimit);
    }

    private List<Route> getWithLiquidityInformation(List<BasicRoute> basicRoutes) {
        return basicRoutes.stream()
                .map(this::getWithLiquidityInformation)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Route getWithLiquidityInformation(BasicRoute basicRoute) {
        List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation = basicRoute.edges().stream()
                .map(edgeComputation::getEdgeWithLiquidityInformation)
                .toList();
        return new Route(edgesWithLiquidityInformation, basicRoute.amount());
    }
}
