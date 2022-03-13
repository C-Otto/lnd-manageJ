package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Policy;
import lnrpc.ChannelEdge;
import lnrpc.ChannelGraph;
import lnrpc.RoutingPolicy;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcGraphTest {
    @Mock
    private GrpcService grpcService;

    @InjectMocks
    private GrpcGraph grpcGraph;

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
                .setNode1Policy(policy(0, 0, true))
                .setNode2Policy(policy(1, 0, false))
                .build();
        DirectedChannelEdge expectedEdge1 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY,
                PUBKEY_2,
                new Policy(0, Coins.NONE, false)
        );
        DirectedChannelEdge expectedEdge2 = new DirectedChannelEdge(
                CHANNEL_ID,
                CAPACITY,
                PUBKEY_2,
                PUBKEY,
                new Policy(1, Coins.NONE, true)
        );
        ChannelEdge edge2 = ChannelEdge.newBuilder()
                .setChannelId(CHANNEL_ID_2.getShortChannelId())
                .setCapacity(CAPACITY_2.satoshis())
                .setNode1Pub(PUBKEY_3.toString())
                .setNode2Pub(PUBKEY_4.toString())
                .setNode1Policy(policy(456, 0, false))
                .setNode2Policy(policy(123, 1, false))
                .build();
        DirectedChannelEdge expectedEdge3 = new DirectedChannelEdge(
                CHANNEL_ID_2,
                CAPACITY_2,
                PUBKEY_3,
                PUBKEY_4,
                new Policy(456, Coins.NONE, true)
        );
        DirectedChannelEdge expectedEdge4 = new DirectedChannelEdge(
                CHANNEL_ID_2,
                CAPACITY_2,
                PUBKEY_4,
                PUBKEY_3,
                new Policy(123, Coins.ofMilliSatoshis(1), true)
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

    private RoutingPolicy policy(int feeRate, int baseFee, boolean disabled) {
        return RoutingPolicy.newBuilder()
                .setFeeRateMilliMsat(feeRate)
                .setFeeBaseMsat(baseFee)
                .setDisabled(disabled)
                .build();
    }
}