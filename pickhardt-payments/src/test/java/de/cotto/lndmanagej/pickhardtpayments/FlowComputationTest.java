package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.MissionControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_DISABLED;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowComputationTest {
    private static final Coins LARGE = Coins.ofSatoshis(10_000_000);
    private static final Coins SMALL = Coins.ofSatoshis(100);

    private FlowComputation flowComputation;

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
        int piecewiseLinearApproximations = 1;
        long quantization = 1;
        flowComputation = new FlowComputation(
                grpcGraph,
                grpcGetInfo,
                channelService,
                balanceService,
                missionControlService,
                quantization,
                piecewiseLinearApproximations
        );
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        lenient().when(missionControlService.getMinimumOfRecentFailures(any(), any())).thenReturn(Optional.empty());
    }

    @Test
    void solve_no_graph() {
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1))).isEqualTo(new Flows());
    }

    @Test
    void solve_edge_disabled() {
        DirectedChannelEdge disabledEdge =
                new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_DISABLED);
        assumeThat(POLICY_DISABLED.enabled()).isFalse();
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(disabledEdge)));
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1))).isEqualTo(new Flows());
    }

    @Test
    void solve() {
        Coins amount = Coins.ofSatoshis(1);
        DirectedChannelEdge enabledEdge = new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY, PUBKEY_2, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(enabledEdge)));
        Flow expectedFlow = new Flow(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_avoids_sending_from_local_channel_lacking_capacity() {
        // TODO use balance of local channel as known balance, not upper bound
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getLocalChannel(CHANNEL_ID_2)).thenReturn(Optional.empty());
        when(balanceService.getAvailableLocalBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(1));
        Coins amount = Coins.ofSatoshis(100);
        DirectedChannelEdge largerButDepletedChannel =
                new DirectedChannelEdge(CHANNEL_ID, LARGE, PUBKEY, PUBKEY_2, POLICY_1);
        DirectedChannelEdge smallerChannel =
                new DirectedChannelEdge(CHANNEL_ID_2, SMALL, PUBKEY, PUBKEY_2, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(
                largerButDepletedChannel,
                smallerChannel
        )));
        Flow expectedFlow = new Flow(new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, SMALL, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_avoids_sending_to_local_channel_lacking_capacity() {
        // TODO use balance of local channel as known balance, not upper bound
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getLocalChannel(CHANNEL_ID_2)).thenReturn(Optional.empty());
        when(balanceService.getAvailableRemoteBalance(CHANNEL_ID)).thenReturn(Coins.ofSatoshis(1));
        Coins amount = Coins.ofSatoshis(100);
        DirectedChannelEdge largerButDepletedChannel =
                new DirectedChannelEdge(CHANNEL_ID, LARGE, PUBKEY_2, PUBKEY, POLICY_1);
        DirectedChannelEdge smallerChannel =
                new DirectedChannelEdge(CHANNEL_ID_2, SMALL, PUBKEY_2, PUBKEY, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(
                largerButDepletedChannel,
                smallerChannel
        )));
        Flow expectedFlow = new Flow(new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY, SMALL, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_2, PUBKEY, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_other_peer_as_start_node() {
        Coins amount = Coins.ofSatoshis(100);
        DirectedChannelEdge edge = new DirectedChannelEdge(CHANNEL_ID, CAPACITY, PUBKEY_2, PUBKEY_3, POLICY_2);
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge)));
        Flow expectedFlow = new Flow(new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, CAPACITY, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_2, PUBKEY_3, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_recent_mission_control_failure_as_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        DirectedChannelEdge edge1a = new DirectedChannelEdge(CHANNEL_ID, LARGE, PUBKEY_2, PUBKEY_3, POLICY_1);
        DirectedChannelEdge edge1b = new DirectedChannelEdge(CHANNEL_ID_2, LARGE, PUBKEY_3, PUBKEY_4, POLICY_1);
        DirectedChannelEdge edge2 = new DirectedChannelEdge(CHANNEL_ID_3, SMALL, PUBKEY_2, PUBKEY_4, POLICY_2);
        when(missionControlService.getMinimumOfRecentFailures(PUBKEY_2, PUBKEY_3))
                .thenReturn(Optional.of(Coins.ofSatoshis(100)));
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge1a, edge1b, edge2)));
        Flow expectedFlow = new Flow(new Edge(CHANNEL_ID_3, PUBKEY_2, PUBKEY_4, SMALL, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_2, PUBKEY_4, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_recent_mission_control_failure_with_higher_amount() {
        Coins amount = Coins.ofSatoshis(100);
        DirectedChannelEdge edge1a = new DirectedChannelEdge(CHANNEL_ID, LARGE, PUBKEY_3, PUBKEY_4, POLICY_2);
        DirectedChannelEdge edge1b = new DirectedChannelEdge(CHANNEL_ID_2, LARGE, PUBKEY_4, PUBKEY, POLICY_2);
        DirectedChannelEdge edge2 = new DirectedChannelEdge(CHANNEL_ID_3, SMALL, PUBKEY_3, PUBKEY, POLICY_2);
        when(missionControlService.getMinimumOfRecentFailures(PUBKEY_3, PUBKEY_4))
                .thenReturn(Optional.of(Coins.ofSatoshis(5_000_000)));
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(edge1a, edge1b, edge2)));
        Flow expectedFlow1 = new Flow(new Edge(CHANNEL_ID, PUBKEY_3, PUBKEY_4, LARGE, POLICY_2), amount);
        Flow expectedFlow2 = new Flow(new Edge(CHANNEL_ID_2, PUBKEY_4, PUBKEY, LARGE, POLICY_2), amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_3, PUBKEY, amount))
                .isEqualTo(new Flows(expectedFlow1, expectedFlow2));
    }
}
