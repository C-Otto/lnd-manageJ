package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.grpc.middleware.GrpcMiddlewareService;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
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
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.RouteHintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.MIN_HTLC_CONSTRAINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.NUM_UPDATES;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OpenInitiator.LOCAL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_WITH_BASE_FEE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdgeComputationTest {
    private static final int MAX_TIME_LOCK_DELTA = 2016;
    private static final int FEE_RATE_WEIGHT = 5;

    @InjectMocks
    private EdgeComputation edgeComputation;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ChannelService channelService;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private GrpcGraph grpcGraph;

    @Mock
    private NodeService nodeService;

    @Mock
    private LiquidityBoundsService liquidityBoundsService;

    @Mock
    private RouteHintService routeHintService;

    @Mock
    private GrpcMiddlewareService grpcMiddlewareService;

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        lenient().when(nodeService.getNode(any())).thenReturn(NODE_PEER);
        lenient().when(liquidityBoundsService.getAssumedLiquidityLowerBound(any())).thenReturn(Coins.NONE);
        lenient().when(grpcMiddlewareService.isConnected()).thenReturn(true);
    }

    @Test
    void no_graph() {
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges()).isEmpty();
    }

    @Test
    void middleware_not_connected() {
        when(grpcMiddlewareService.isConnected()).thenReturn(false);
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges()).isEmpty();
    }

    @Test
    void does_not_add_edge_for_disabled_channel() {
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_DISABLED, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges()).isEmpty();
    }

    @Test
    void does_not_add_edge_exceeding_maximum_time_lock_delta() {
        int edgeDelta = 40;
        int maximumTimeLockDelta = edgeDelta - 1;
        Policy policy =
                new Policy(0, Coins.NONE, true, edgeDelta, Coins.ofMilliSatoshis(1), Coins.ofSatoshis(10_000));
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, maximumTimeLockDelta).edges()).isEmpty();
    }

    @Test
    void does_not_add_edge_with_fee_rate_at_or_above_limit() {
        int feeRateLimit = 199;
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRateLimit);
        Policy policyExpensive = policy(200);
        // needs to be excluded to avoid sending top-up payments in a tiny loop: S-X-S
        Policy policyAtLimit = policy(199);
        Policy policyOk = policy(198);
        Edge edgeExpensive =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policyExpensive, Policy.UNKNOWN);
        Edge edgeAtLimit =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policyAtLimit, Policy.UNKNOWN);
        Edge edgeOk =
                new Edge(CHANNEL_ID_3, PUBKEY, PUBKEY_2, CAPACITY, policyOk, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edgeExpensive, edgeAtLimit, edgeOk)));
        assertThat(
                edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA).edges().stream()
                        .map(EdgeWithLiquidityInformation::channelId)
        ).containsExactly(CHANNEL_ID_3);
    }

    @Test
    void adds_edge_with_fee_rate_at_limit_if_first_hop_and_fee_is_ignored() {
        int feeRateLimit = 1000;
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRateLimit);
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY_4, PUBKEY_2, CAPACITY, policy(feeRateLimit), Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA).edges()).isNotEmpty();
    }

    @Test
    void does_not_add_edge_with_fee_rate_at_limit_if_first_hop_and_fee_is_not_ignored() {
        long feeRateLimit = 2000L;
        PaymentOptions paymentOptions = new PaymentOptions(
                Optional.of(0),
                Optional.of(feeRateLimit),
                Optional.empty(),
                false,
                Optional.empty(),
                Optional.empty()
        );
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY_4, PUBKEY, CAPACITY, policy(feeRateLimit), Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA).edges()).isEmpty();
    }

    @Test
    void does_not_add_first_hop_edge_with_fee_rate_at_or_above_limit_for_first_hops() {
        Pubkey ownPubkey = EDGE.startNode();
        Pubkey topUpPeer = PUBKEY_4;
        int feeRateLimit = 200;
        int feeRateLimitForFirstHops = 100;

        when(grpcGetInfo.getPubkey()).thenReturn(ownPubkey);
        PaymentOptions paymentOptions =
                PaymentOptions.forTopUp(FEE_RATE_WEIGHT, feeRateLimit, feeRateLimitForFirstHops, topUpPeer);
        Policy lastHopPolicy = policy(199);
        Policy firstHopPolicyExpensive = policy(100);
        Policy firstHopPolicyOk = policy(99);
        Edge lastHop = new Edge(
                CHANNEL_ID,
                topUpPeer,
                ownPubkey,
                CAPACITY,
                lastHopPolicy,
                Policy.UNKNOWN
        );
        Edge firstHopExpensive = new Edge(
                CHANNEL_ID_2,
                ownPubkey,
                PUBKEY_2,
                CAPACITY,
                firstHopPolicyExpensive,
                Policy.UNKNOWN
        );
        Edge firstHopOk = new Edge(
                CHANNEL_ID_3,
                ownPubkey,
                PUBKEY_2,
                CAPACITY,
                firstHopPolicyOk,
                Policy.UNKNOWN
        );
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(lastHop, firstHopExpensive, firstHopOk)));
        assertThat(
                edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA).edges().stream()
                        .map(EdgeWithLiquidityInformation::channelId)
        ).containsExactlyInAnyOrder(CHANNEL_ID, CHANNEL_ID_3);
    }

    @Test
    void adds_first_hop_edge_if_limit_for_first_hops_is_not_specified() {
        Pubkey ownPubkey = EDGE.startNode();
        int feeRateLimit = 200;

        when(grpcGetInfo.getPubkey()).thenReturn(ownPubkey);
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRateLimit);
        Policy firstHopPolicyExpensive = policy(100);
        Edge firstHopExpensiveButOk = new Edge(
                CHANNEL_ID_2,
                ownPubkey,
                PUBKEY_2,
                CAPACITY,
                firstHopPolicyExpensive,
                Policy.UNKNOWN
        );
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(firstHopExpensiveButOk)));
        assertThat(
                edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA).edges().stream()
                        .map(EdgeWithLiquidityInformation::channelId)
        ).containsExactlyInAnyOrder(CHANNEL_ID_2);
    }

    @Test
    void adds_edge_for_channel_with_base_fee() {
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_WITH_BASE_FEE, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges()).isNotEmpty();
    }

    @Test
    void adds_liquidity_information_for_local_channel_as_source() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());
        when(channelService.getOpenChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = Coins.ofSatoshis(4_567);
        when(balanceService.getAvailableLocalBalance(EDGE.channelId())).thenReturn(knownLiquidity);
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(Coins.ofSatoshis(5_000));

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void considers_max_htlc_policy_for_local_channel() {
        Coins maxHtlc = Coins.ofSatoshis(1_234_567);
        Policy localPolicy = new Policy(
                200,
                Coins.NONE,
                true,
                40,
                Coins.ofSatoshis(159),
                maxHtlc
        );
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, localPolicy, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        when(grpcGetInfo.getPubkey()).thenReturn(edge.startNode());
        when(channelService.getOpenChannel(edge.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = maxHtlc.add(Coins.ofSatoshis(5_000_000));
        when(balanceService.getAvailableLocalBalance(edge.channelId())).thenReturn(knownLiquidity);
        when(balanceService.getAvailableRemoteBalance(edge.channelId())).thenReturn(Coins.ofSatoshis(5_000));

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(edge, maxHtlc));
    }

    @Test
    void considers_max_htlc_policy_for_remote_channel() {
        Coins maxHtlc = Coins.ofSatoshis(1_234_567);
        Policy remotePolicy = new Policy(
                200,
                Coins.NONE,
                true,
                40,
                Coins.ofSatoshis(159),
                maxHtlc
        );
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY, CAPACITY, remotePolicy, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        when(grpcGetInfo.getPubkey()).thenReturn(edge.endNode());
        when(channelService.getOpenChannel(edge.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = maxHtlc.add(Coins.ofSatoshis(5_000_000));
        when(balanceService.getAvailableRemoteBalance(edge.channelId())).thenReturn(knownLiquidity);

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(edge, maxHtlc));
    }

    @Test
    void ignores_local_channels_with_lots_of_local_liquidity_but_almost_nothing_on_remote_side() {
        // https://github.com/lightningnetwork/lnd/issues/7108
        mockEdge();
        ChannelId channelId = EDGE.channelId();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());
        when(channelService.getOpenChannel(channelId)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        // as observed on 9 May 2023:
        // Reducing local balance (from x mSAT to 353999 mSAT): remote side does not have enough
        // funds (4222568 mSAT < 4223000 mSAT) to pay for non-dust HTLC in case of unilateral close.
        when(balanceService.getAvailableRemoteBalance(channelId)).thenReturn(Coins.ofMilliSatoshis(4_222_999));
        lenient().when(balanceService.getAvailableLocalBalance(channelId)).thenReturn(Coins.ofSatoshis(1_000_000));

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    @Test
    void adds_edge_from_route_hint_service() {
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of()));
        Coins oneMilliSatoshi = Coins.ofMilliSatoshis(1);
        Coins fiftyCoins = Coins.ofSatoshis(5_000_000_000L);
        Policy policy = new Policy(200, Coins.NONE, true, 40, oneMilliSatoshi, fiftyCoins);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, fiftyCoins, policy, Policy.UNKNOWN);
        when(routeHintService.getEdgesFromPaymentHints()).thenReturn(Set.of(
                new Edge(
                        edge.channelId(),
                        edge.startNode(),
                        edge.endNode(),
                        edge.capacity(),
                        edge.policy(),
                        edge.reversePolicy()
                )
        ));
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(edge, fiftyCoins));
    }

    @Test
    void local_channel_not_found_as_end_node() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    @Test
    void reduces_liquidity_to_zero_for_inactive_channel_as_last_hop() {
        mockEdge();
        mockInactiveChannel();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
        verify(balanceService, never()).getAvailableLocalBalance(EDGE.channelId());
    }

    @Test
    void adds_liquidity_information_for_local_channel_as_target() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());
        when(channelService.getOpenChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = Coins.ofSatoshis(4_567);
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(knownLiquidity);

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void local_channel_not_found_as_start_node() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    // CPD-OFF
    @Test
    void reduces_liquidity_to_zero_for_inactive_channel_as_first_hop() {
        mockEdge();
        mockInactiveChannel();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());

        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
        verify(balanceService, never()).getAvailableLocalBalance(EDGE.channelId());
    }
    // CPD-ON

    @Test
    void adds_upper_bound_from_liquidity_bounds_service() {
        mockEdge();
        Coins upperBound = Coins.ofSatoshis(100);
        mockUpperBound(upperBound);
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound));
    }

    @Test
    void default_if_no_liquidity_information_is_known() {
        mockEdge();
        assertThat(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA).edges())
                .contains(EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity()));
    }

    @Test
    void ignores_other_peers_if_peer_for_first_hop_is_set() {
        Pubkey ownNode = PUBKEY_4;
        Pubkey peerForFirstHop = PUBKEY_3;

        Edge edgeToFirstHop =
                new Edge(CHANNEL_ID, ownNode, peerForFirstHop, CAPACITY, POLICY_1, Policy.UNKNOWN);
        Edge otherEdge =
                new Edge(CHANNEL_ID, ownNode, PUBKEY, CAPACITY, POLICY_1, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edgeToFirstHop, otherEdge)));

        PaymentOptions paymentOptions =
                PaymentOptions.forTopUp(FEE_RATE_WEIGHT, 999, 0, PUBKEY_2, peerForFirstHop);
        EdgesWithLiquidityInformation edges = edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA);

        assertThat(edges.edges().stream().map(EdgeWithLiquidityInformation::endNode)).containsExactly(peerForFirstHop);
    }

    @Test
    void ignores_edge_to_last_hop() {
        Pubkey ownNode = PUBKEY_4;
        Pubkey lastHopNode = PUBKEY_2;

        Edge edgeToLastHop =
                new Edge(CHANNEL_ID, ownNode, lastHopNode, CAPACITY, POLICY_1, Policy.UNKNOWN);
        Edge otherEdge =
                new Edge(CHANNEL_ID, ownNode, PUBKEY, CAPACITY, POLICY_1, Policy.UNKNOWN);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edgeToLastHop, otherEdge)));

        PaymentOptions paymentOptions =
                PaymentOptions.forTopUp(FEE_RATE_WEIGHT, 999, 0, lastHopNode);
        EdgesWithLiquidityInformation edges = edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA);

        assertThat(edges.edges().stream().map(EdgeWithLiquidityInformation::endNode)).containsExactly(PUBKEY);
    }

    @Test
    void getEdgeWithLiquidityInformation_default() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity()));
    }

    @Test
    void getEdgeWithLiquidityInformation_first_node_is_own_node() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        Coins knownLiquidity = Coins.ofSatoshis(4_567);
        when(channelService.getOpenChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getAvailableLocalBalance(EDGE.channelId())).thenReturn(knownLiquidity);
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(Coins.ofSatoshis(5_000));
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void getEdgeWithLiquidityInformation_first_node_is_own_node_but_channel_is_inactive() {
        mockInactiveChannel();
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    @Test
    void getEdgeWithLiquidityInformation_second_node_is_own_node() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_2);
        Coins knownLiquidity = Coins.ofSatoshis(4_567);
        when(channelService.getOpenChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(knownLiquidity);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_upper_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins upperBound = Coins.ofSatoshis(455);
        mockUpperBound(upperBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_upper_bound_above_capacity() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins upperBound = EDGE.capacity().add(Coins.ofSatoshis(1));
        mockUpperBound(upperBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity()));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_lower_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins lowerBound = Coins.ofSatoshis(455);
        mockLowerBound(lowerBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forLowerBound(EDGE, lowerBound));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_lower_and_upper_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins lowerBound = Coins.ofSatoshis(100);
        Coins upperBound = Coins.ofSatoshis(455);
        mockLowerBound(lowerBound);
        mockUpperBound(upperBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forLowerAndUpperBound(EDGE, lowerBound, upperBound));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_conflicting_lower_and_upper_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins lowerBound = Coins.ofSatoshis(1_000);
        Coins upperBound = Coins.ofSatoshis(999);
        mockLowerBound(lowerBound);
        mockUpperBound(upperBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forLowerAndUpperBound(EDGE, lowerBound, lowerBound));
    }

    private void mockLowerBound(Coins lowerBound) {
        when(liquidityBoundsService.getAssumedLiquidityLowerBound(EDGE)).thenReturn(lowerBound);
    }

    private void mockUpperBound(Coins upperBound) {
        when(liquidityBoundsService.getAssumedLiquidityUpperBound(EDGE)).thenReturn(Optional.of(upperBound));
    }

    private void mockEdge() {
        Edge edge =
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
    }

    private static Policy policy(long feeRate) {
        return new Policy(feeRate, Coins.NONE, true, 40, Coins.NONE, Coins.NONE);
    }

    private void mockInactiveChannel() {
        LocalOpenChannel inactiveChannel = new LocalOpenChannel(
                new ChannelCoreInformation(CHANNEL_ID, CHANNEL_POINT, CAPACITY),
                PUBKEY,
                PUBKEY_2,
                BALANCE_INFORMATION,
                LOCAL,
                TOTAL_SENT,
                TOTAL_RECEIVED,
                false,
                false,
                NUM_UPDATES,
                MIN_HTLC_CONSTRAINT
        );
        when(channelService.getOpenChannel(EDGE.channelId())).thenReturn(Optional.of(inactiveChannel));
    }
}
