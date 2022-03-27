package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowComputationTest {
    private static final Coins LARGE = Coins.ofSatoshis(10_000_000);
    private static final Coins SMALL = Coins.ofSatoshis(100);

    private FlowComputation flowComputation;

    @Mock
    private EdgeComputation edgeComputation;

    @BeforeEach
    void setUp() {
        int piecewiseLinearApproximations = 1;
        long quantization = 1;
        flowComputation = new FlowComputation(
                edgeComputation,
                quantization,
                piecewiseLinearApproximations
        );
    }

    @Test
    void solve_no_graph() {
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1))).isEqualTo(new Flows());
    }

    @Test
    void solve_no_edge() {
        when(edgeComputation.getEdges()).thenReturn(Set.of());
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1))).isEqualTo(new Flows());
    }

    @Test
    void solve() {
        Coins amount = Coins.ofSatoshis(1);
        EdgeWithLiquidityInformation edge = EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity());
        when(edgeComputation.getEdges()).thenReturn(Set.of(edge));
        Flow expectedFlow = new Flow(EDGE, amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_avoids_sending_from_depleted_local_channel() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, LARGE, POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, SMALL, POLICY_2);
        when(edgeComputation.getEdges()).thenReturn(Set.of(
                EdgeWithLiquidityInformation.forKnownLiquidity(edge1, Coins.NONE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Coins amount = Coins.ofSatoshis(100);
        Flow expectedFlow = new Flow(edge2, amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_channel_with_small_liquidity_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        Edge edge1a = new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, LARGE, POLICY_1);
        Edge edge1b = new Edge(CHANNEL_ID_2, PUBKEY_3, PUBKEY_4, LARGE, POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_3, PUBKEY_2, PUBKEY_4, SMALL, POLICY_2);
        when(edgeComputation.getEdges()).thenReturn(Set.of(
                EdgeWithLiquidityInformation.forUpperBound(edge1a, amount),
                EdgeWithLiquidityInformation.forUpperBound(edge1b, LARGE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Flow expectedFlow = new Flow(edge2, amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_2, PUBKEY_4, amount)).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_channel_with_large_liquidity_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        Edge edge1a = new Edge(CHANNEL_ID, PUBKEY_3, PUBKEY_4, LARGE, POLICY_2);
        Edge edge1b = new Edge(CHANNEL_ID_2, PUBKEY_4, PUBKEY, LARGE, POLICY_2);
        Edge edge2 = new Edge(CHANNEL_ID_3, PUBKEY_3, PUBKEY, SMALL, POLICY_1);
        when(edgeComputation.getEdges()).thenReturn(Set.of(
                EdgeWithLiquidityInformation.forUpperBound(edge1a, Coins.ofSatoshis(5_000_000)),
                EdgeWithLiquidityInformation.forUpperBound(edge1b, LARGE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Flow expectedFlow1 = new Flow(edge1a, amount);
        Flow expectedFlow2 = new Flow(edge1b, amount);
        assertThat(flowComputation.getOptimalFlows(PUBKEY_3, PUBKEY, amount))
                .isEqualTo(new Flows(expectedFlow1, expectedFlow2));
    }

}
