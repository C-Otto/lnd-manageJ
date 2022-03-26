package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentSplitterTest {
    private static final Coins AMOUNT = Coins.ofSatoshis(1_234);
    @InjectMocks
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private FlowComputation flowComputation;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @BeforeEach
    void setUp() {
        when(flowComputation.getOptimalFlows(any(), any(), any())).thenReturn(new Flows());
    }

    @Test
    void getMultiPathPaymentTo_uses_own_pubkey_as_source() {
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
        multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY_2, AMOUNT);
        verify(flowComputation).getOptimalFlows(PUBKEY_4, PUBKEY_2, AMOUNT);
    }

    @Test
    void getMultiPathPayment_failure() {
        MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, AMOUNT);
        assertThat(multiPathPayment.probability()).isZero();
    }

    @Test
    void getMultiPathPaymentTo() {
        when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, AMOUNT)).thenReturn(new Flows(FLOW));
        when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);
        MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY_2, AMOUNT);
        MultiPathPayment expected = new MultiPathPayment(Set.of(new Route(List.of(EDGE), AMOUNT)));
        assertThat(multiPathPayment).isEqualTo(expected);
    }

    @Test
    void getMultiPathPayment_one_flow_probability() {
        long capacitySat = EDGE.capacity().satoshis();
        Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
        Flow flow = new Flow(EDGE, halfOfCapacity);
        when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, halfOfCapacity)).thenReturn(new Flows(flow));

        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, halfOfCapacity);

        assertThat(multiPathPayment.probability())
                .isEqualTo((1.0 * halfOfCapacity.satoshis() + 1) / (capacitySat + 1));
    }

    @Test
    void getMultiPathPayment_two_flows_probability() {
        long capacitySat = CAPACITY.satoshis();
        Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
        Flow flow1 = new Flow(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1), halfOfCapacity);
        Flow flow2 = new Flow(new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY_2, POLICY_1), CAPACITY_2);
        Coins totalAmount = halfOfCapacity.add(CAPACITY_2);
        when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, totalAmount)).thenReturn(new Flows(flow1, flow2));

        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, totalAmount);

        double probabilityFlow1 = (1.0 * halfOfCapacity.satoshis() + 1) / (capacitySat + 1);
        double probabilityFlow2 = 1.0 / (CAPACITY_2.satoshis() + 1);
        assertThat(multiPathPayment.probability()).isEqualTo(probabilityFlow1 * probabilityFlow2);
    }

    @Test
    void getMultiPathPayment_two_flows_through_same_channel_probability() {
        long capacitySat = EDGE.capacity().satoshis();
        Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
        Flow flow1 = new Flow(EDGE, halfOfCapacity);
        Flow flow2 = new Flow(EDGE, halfOfCapacity);
        when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, EDGE.capacity())).thenReturn(new Flows(flow1, flow2));

        MultiPathPayment multiPathPayment =
                multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, EDGE.capacity());

        assertThat(multiPathPayment.probability()).isEqualTo(1.0 / (capacitySat + 1));
    }

    @Test
    void getMultiPathPayment_adds_remainder_to_route() {
        when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, AMOUNT)).thenReturn(new Flows(FLOW));
        assumeThat(FLOW.amount()).isLessThan(AMOUNT);
        MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(PUBKEY, PUBKEY_2, AMOUNT);
        assertThat(multiPathPayment.routes().iterator().next().amount()).isEqualTo(AMOUNT);
    }
}
