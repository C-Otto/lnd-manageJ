package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Policy;
import lnrpc.ChannelEdge;
import lnrpc.ChannelGraph;
import lnrpc.RoutingPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcGraphTest {
    private static final Coins MAX_HTLC = Coins.ofMilliSatoshis(5_432);
    @InjectMocks
    private GrpcGraph grpcGraph;

    @Mock
    private GrpcService grpcService;

    @Mock
    private GrpcPolicy grpcPolicy;

    @BeforeEach
    void setUp() {
        lenient().when(grpcPolicy.toPolicy(any())).thenCallRealMethod();
    }

    @Test
    void empty() {
        when(grpcService.describeGraph()).thenReturn(Optional.empty());
        assertThat(grpcGraph.getChannelEdges()).isEmpty();
    }

    @Test
    void no_edge() {
        ChannelGraph channelGraph = ChannelGraph.getDefaultInstance();
        when(grpcService.describeGraph()).thenReturn(Optional.of(channelGraph));
        assertThat(grpcGraph.getChannelEdges()).contains(Set.of());
    }

    @Test
    void two_edges() {
        ChannelEdge edge1 = ChannelEdge.newBuilder()
                .setChannelId(CHANNEL_ID.getShortChannelId())
                .setCapacity(CAPACITY.satoshis())
                .setNode1Pub(PUBKEY.toString())
                .setNode2Pub(PUBKEY_2.toString())
                .setNode1Policy(policy(0, 0, true, 40))
                .setNode2Policy(policy(1, 0, false, 144))
                .build();
        DirectedChannelEdge expectedEdge1 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY,
                PUBKEY_2,
                new Policy(0, Coins.NONE, false, 40, MAX_HTLC)
        );
        DirectedChannelEdge expectedEdge2 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY_2,
                PUBKEY,
                new Policy(1, Coins.NONE, true, 144, MAX_HTLC)
        );
        ChannelEdge edge2 = ChannelEdge.newBuilder()
                .setChannelId(CHANNEL_ID_2.getShortChannelId())
                .setCapacity(CAPACITY_2.satoshis())
                .setNode1Pub(PUBKEY_3.toString())
                .setNode2Pub(PUBKEY_4.toString())
                .setNode1Policy(policy(456, 0, false, 123))
                .setNode2Policy(policy(123, 1, false, 456))
                .build();
        DirectedChannelEdge expectedEdge3 = new DirectedChannelEdge(
                CHANNEL_ID_2,
                CAPACITY_2,
                PUBKEY_3,
                PUBKEY_4,
                new Policy(456, Coins.NONE, true, 123, MAX_HTLC)
        );
        DirectedChannelEdge expectedEdge4 = new DirectedChannelEdge(
                CHANNEL_ID_2,
                CAPACITY_2,
                PUBKEY_4,
                PUBKEY_3,
                new Policy(123, Coins.ofMilliSatoshis(1), true, 456, MAX_HTLC)
        );
        ChannelGraph channelGraph = ChannelGraph.newBuilder()
                .addEdges(edge1)
                .addEdges(edge2)
                .build();
        when(grpcService.describeGraph()).thenReturn(Optional.of(channelGraph));
        assertThat(grpcGraph.getChannelEdges().orElseThrow()).containsExactlyInAnyOrder(
                expectedEdge1, expectedEdge2, expectedEdge3, expectedEdge4
        );
    }

    @Test
    void missing_policy_results_in_disabled_channel() {
        ChannelEdge edgeWithMissingPolicy = ChannelEdge.newBuilder()
                .setChannelId(CHANNEL_ID.getShortChannelId())
                .setCapacity(CAPACITY.satoshis())
                .setNode1Pub(PUBKEY.toString())
                .setNode2Pub(PUBKEY_2.toString())
                .build();
        DirectedChannelEdge expectedPolicyForNode1 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY,
                PUBKEY_2,
                new Policy(0, Coins.NONE, false, 0, Coins.NONE)
        );
        DirectedChannelEdge expectedPolicyForNode2 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY_2,
                PUBKEY,
                new Policy(0, Coins.NONE, false, 0, Coins.NONE)
        );
        ChannelGraph channelGraph = ChannelGraph.newBuilder().addEdges(edgeWithMissingPolicy).build();
        when(grpcService.describeGraph()).thenReturn(Optional.of(channelGraph));
        assertThat(grpcGraph.getChannelEdges().orElseThrow())
                .containsExactlyInAnyOrder(expectedPolicyForNode1, expectedPolicyForNode2);
    }

    private RoutingPolicy policy(int feeRate, int baseFee, boolean disabled, int timeLockDelta) {
        return RoutingPolicy.newBuilder()
                .setFeeRateMilliMsat(feeRate)
                .setFeeBaseMsat(baseFee)
                .setDisabled(disabled)
                .setTimeLockDelta(timeLockDelta)
                .setMaxHtlcMsat(5_432)
                .build();
    }
}
