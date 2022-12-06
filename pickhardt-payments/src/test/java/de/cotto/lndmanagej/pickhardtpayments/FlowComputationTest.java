package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.PIECEWISE_LINEAR_APPROXIMATIONS;
import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.QUANTIZATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlowComputationTest {
    private static final Coins LARGE = Coins.ofSatoshis(10_000_001);
    private static final Coins SMALL = Coins.ofSatoshis(101);
    private static final int MAX_TIME_LOCK_DELTA = 2016;

    private FlowComputation flowComputation;

    @Mock
    private EdgeComputation edgeComputation;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        when(configurationService.getIntegerValue(QUANTIZATION)).thenReturn(Optional.of(1));
        when(configurationService.getIntegerValue(PIECEWISE_LINEAR_APPROXIMATIONS)).thenReturn(Optional.of(1));
        flowComputation = new FlowComputation(edgeComputation, grpcGetInfo, configurationService);
        lenient().when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
    }

    @Test
    void solve_no_edge() {
        when(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA))
                .thenReturn(EdgesWithLiquidityInformation.EMPTY);
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY,
                PUBKEY_2,
                Coins.ofSatoshis(1),
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows());
    }

    @Test
    void passes_fee_rate_limit_to_get_edges() {
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(123);
        when(edgeComputation.getEdges(paymentOptions, MAX_TIME_LOCK_DELTA))
                .thenReturn(EdgesWithLiquidityInformation.EMPTY);
        flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1), paymentOptions, MAX_TIME_LOCK_DELTA);
        verify(edgeComputation).getEdges(paymentOptions, MAX_TIME_LOCK_DELTA);
    }

    @Test
    void passes_maximum_time_lock_delta_for_edge_computation() {
        int maxTimeLockDelta = 1234;
        PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(123);
        when(edgeComputation.getEdges(paymentOptions, maxTimeLockDelta))
                .thenReturn(EdgesWithLiquidityInformation.EMPTY);
        flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, Coins.ofSatoshis(1), paymentOptions, maxTimeLockDelta);
        verify(edgeComputation).getEdges(paymentOptions, maxTimeLockDelta);
    }

    @Test
    void solve() {
        Coins amount = Coins.ofSatoshis(1);
        EdgeWithLiquidityInformation edge = EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity());
        when(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA))
                .thenReturn(new EdgesWithLiquidityInformation(edge));
        Flow expectedFlow = new Flow(EDGE, amount);
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY,
                PUBKEY_2,
                amount,
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_amount_below_quantization() {
        when(configurationService.getIntegerValue(QUANTIZATION)).thenReturn(Optional.of(10));
        Coins amount = Coins.ofSatoshis(9);
        EdgeWithLiquidityInformation edge = EdgeWithLiquidityInformation.forUpperBound(EDGE, EDGE.capacity());
        when(edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA))
                .thenReturn(new EdgesWithLiquidityInformation(edge));
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY,
                PUBKEY_2,
                amount,
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows(new Flow(EDGE, amount)));
    }

    @Test
    void solve_avoids_sending_from_depleted_local_channel() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, LARGE, POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, SMALL, POLICY_2);
        when(
                edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA)
        ).thenReturn(new EdgesWithLiquidityInformation(
                EdgeWithLiquidityInformation.forKnownLiquidity(edge1, Coins.NONE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Coins amount = Coins.ofSatoshis(100);
        Flow expectedFlow = new Flow(edge2, amount);
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY,
                PUBKEY_2,
                amount,
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_channel_with_small_liquidity_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        Edge edge1a = new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, LARGE, POLICY_1);
        Edge edge1b = new Edge(CHANNEL_ID_2, PUBKEY_3, PUBKEY_4, LARGE, POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_3, PUBKEY_2, PUBKEY_4, SMALL, POLICY_2);
        when(
                edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA)
        ).thenReturn(new EdgesWithLiquidityInformation(
                EdgeWithLiquidityInformation.forUpperBound(edge1a, amount),
                EdgeWithLiquidityInformation.forUpperBound(edge1b, LARGE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Flow expectedFlow = new Flow(edge2, amount);
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY_2,
                PUBKEY_4,
                amount,
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows(expectedFlow));
    }

    @Test
    void solve_with_channel_with_large_liquidity_upper_bound() {
        Coins amount = Coins.ofSatoshis(100);
        Edge edge1a = new Edge(CHANNEL_ID, PUBKEY_3, PUBKEY_4, LARGE, POLICY_2);
        Edge edge1b = new Edge(CHANNEL_ID_2, PUBKEY_4, PUBKEY, LARGE, POLICY_2);
        Edge edge2 = new Edge(CHANNEL_ID_3, PUBKEY_3, PUBKEY, SMALL, POLICY_1);
        when(
                edgeComputation.getEdges(DEFAULT_PAYMENT_OPTIONS, MAX_TIME_LOCK_DELTA)
        ).thenReturn(new EdgesWithLiquidityInformation(
                EdgeWithLiquidityInformation.forUpperBound(edge1a, Coins.ofSatoshis(5_000_000)),
                EdgeWithLiquidityInformation.forUpperBound(edge1b, LARGE),
                EdgeWithLiquidityInformation.forUpperBound(edge2, SMALL)
        ));
        Flow expectedFlow1 = new Flow(edge1a, amount);
        Flow expectedFlow2 = new Flow(edge1b, amount);
        assertThat(flowComputation.getOptimalFlows(
                PUBKEY_3,
                PUBKEY,
                amount,
                DEFAULT_PAYMENT_OPTIONS,
                MAX_TIME_LOCK_DELTA
        )).isEqualTo(new Flows(expectedFlow1, expectedFlow2));
    }

}
