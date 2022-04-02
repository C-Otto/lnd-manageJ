package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.MissionControlService;
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
    private MissionControlService missionControlService;

    @BeforeEach
    void setUp() {
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        lenient().when(missionControlService.getMinimumOfRecentFailures(any(), any())).thenReturn(Optional.empty());
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
    void adds_upper_bound_from_mission_control() {
        mockEdge();
        when(missionControlService.getMinimumOfRecentFailures(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(Coins.ofSatoshis(100)));
        assertThat(edgeComputation.getEdges().edges())
                .contains(EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(99)));
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
    void getEdgeWithLiquidityInformation_with_data_from_mission_control() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        Coins recentFailureAmount = Coins.ofSatoshis(456);
        Coins upperBound = Coins.ofSatoshis(455);
        when(missionControlService.getMinimumOfRecentFailures(EDGE.startNode(), EDGE.endNode()))
                .thenReturn(Optional.of(recentFailureAmount));
        assertThat(edgeComputation.getEdgeWithLiquidityInformation(EDGE))
                .isEqualTo(EdgeWithLiquidityInformation.forUpperBound(EDGE, upperBound));
    }

    private void mockEdge() {
        DirectedChannelEdge edge = new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_1);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
    }
}
