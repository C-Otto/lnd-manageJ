package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.LiquidityBoundsService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_WITH_BASE_FEE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EdgeComputationTest {
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

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        lenient().when(nodeService.getNode(any())).thenReturn(NODE_PEER);
        lenient().when(liquidityBoundsService.getAssumedLiquidityLowerBound(any(), any())).thenReturn(Coins.NONE);
    }

    @Test
    void no_graph() {
        assertThat(edgeComputation.getEdges().edges()).isEmpty();
    }

    @Test
    void does_not_add_edge_for_disabled_channel() {
        DirectedChannelEdge edge = new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_DISABLED);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges().edges()).isEmpty();
    }

    @Test
    void does_not_add_edge_for_channel_with_base_fee() {
        DirectedChannelEdge edge =
                new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_WITH_BASE_FEE);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        assertThat(edgeComputation.getEdges().edges()).isEmpty();
    }

    @Test
    void adds_liquidity_information_for_local_channel_as_source() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());
        when(channelService.getLocalChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = Coins.ofSatoshis(456);
        when(balanceService.getAvailableLocalBalance(EDGE.channelId())).thenReturn(knownLiquidity);

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void local_channel_not_found_as_end_node() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    @Test
    void reduces_liquidity_to_zero_for_offline_peer_as_end_node() {
        mockEdge();
        mockOfflinePeer();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.startNode());

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
        verify(balanceService, never()).getAvailableLocalBalance(EDGE.channelId());
    }

    @Test
    void adds_liquidity_information_for_local_channel_as_target() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());
        when(channelService.getLocalChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        Coins knownLiquidity = Coins.ofSatoshis(456);
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(knownLiquidity);

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void local_channel_not_found_as_start_node() {
        mockEdge();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
    }

    @Test
    void reduces_liquidity_to_zero_for_offline_peer_as_start_node() {
        mockEdge();
        mockOfflinePeer();
        when(grpcGetInfo.getPubkey()).thenReturn(EDGE.endNode());

        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.NONE));
        verify(balanceService, never()).getAvailableLocalBalance(EDGE.channelId());
    }

    private void mockOfflinePeer() {
        Pubkey remotePubkey = LOCAL_OPEN_CHANNEL.getRemotePubkey();
        when(nodeService.getNode(remotePubkey)).thenReturn(new Node(remotePubkey, "", 0, false));
        when(channelService.getLocalChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
    }

    @Test
    void adds_upper_bound_from_liquidity_bounds_service() {
        mockEdge();
        Coins upperBound = Coins.ofSatoshis(100);
        when(liquidityBoundsService.getAssumedLiquidityUpperBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(upperBound));
        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound));
    }

    @Test
    void default_if_no_liquidity_information_is_known() {
        mockEdge();
        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity()));
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
        Coins knownLiquidity = Coins.ofSatoshis(456);
        when(channelService.getLocalChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getAvailableLocalBalance(EDGE.channelId())).thenReturn(knownLiquidity);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void getEdgeWithLiquidityInformation_second_node_is_own_node() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_2);
        Coins knownLiquidity = Coins.ofSatoshis(456);
        when(channelService.getLocalChannel(EDGE.channelId())).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(balanceService.getAvailableRemoteBalance(EDGE.channelId())).thenReturn(knownLiquidity);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, knownLiquidity));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_upper_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins upperBound = Coins.ofSatoshis(455);
        when(liquidityBoundsService.getAssumedLiquidityUpperBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(upperBound));
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_upper_bound_above_capacity() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins upperBound = EDGE.capacity().add(Coins.ofSatoshis(1));
        when(liquidityBoundsService.getAssumedLiquidityUpperBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(upperBound));
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity()));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_lower_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins lowerBound = Coins.ofSatoshis(455);
        when(liquidityBoundsService.getAssumedLiquidityLowerBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(lowerBound);
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forLowerBound(EDGE, lowerBound));
    }

    @Test
    void getEdgeWithLiquidityInformation_with_lower_and_upper_bound() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins lowerBound = Coins.ofSatoshis(100);
        Coins upperBound = Coins.ofSatoshis(455);
        when(liquidityBoundsService.getAssumedLiquidityLowerBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(lowerBound);
        when(liquidityBoundsService.getAssumedLiquidityUpperBound(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(upperBound));
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forLowerAndUpperBound(EDGE, lowerBound, upperBound));
    }

    private void mockEdge() {
        DirectedChannelEdge edge = new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_1);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
    }
}
