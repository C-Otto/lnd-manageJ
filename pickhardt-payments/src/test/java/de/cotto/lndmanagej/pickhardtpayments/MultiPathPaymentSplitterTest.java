package de.cotto.lndmanagej.pickhardtpayments;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.grpc.GrpcGetInfo;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.Flow;
import de.cotto.lndmanagej.pickhardtpayments.model.Flows;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.PickhardtPaymentsConfigurationSettings.MAX_CLTV_EXPIRY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW;
import static de.cotto.lndmanagej.pickhardtpayments.model.PaymentOptions.DEFAULT_PAYMENT_OPTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MultiPathPaymentSplitterTest {
    private static final Coins AMOUNT = Coins.ofSatoshis(1_234);
    private static final int FINAL_CLTV_DELTA = 40;
    private static final int DEFAULT_MAX_CLTV_EXPIRY = 2016;

    @InjectMocks
    private MultiPathPaymentSplitter multiPathPaymentSplitter;

    @Mock
    private FlowComputation flowComputation;

    @Mock
    private GrpcGetInfo grpcGetInfo;

    @Mock
    private EdgeComputation edgeComputation;

    @Mock
    private ChannelService channelService;

    @Mock
    private PolicyService policyService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private CltvService cltvService;

    @BeforeEach
    void setUp() {
        when(flowComputation.getOptimalFlows(any(), any(), any(), any(), anyInt())).thenReturn(new Flows());
        lenient().when(edgeComputation.getEdgeWithLiquidityInformation(EDGE)).thenReturn(noInformationFor(EDGE));
        lenient().when(cltvService.getMaximumDeltaForEdges(anyInt(), anyInt(), any(), any()))
                .thenReturn(DEFAULT_MAX_CLTV_EXPIRY);
    }

    @Nested
    class GetMultiPathPaymentTo {
        @Test
        void uses_own_pubkey_as_source() {
            when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
            multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY_2, AMOUNT, DEFAULT_PAYMENT_OPTIONS, FINAL_CLTV_DELTA);
            verify(flowComputation)
                    .getOptimalFlows(PUBKEY_4, PUBKEY_2, AMOUNT, DEFAULT_PAYMENT_OPTIONS, DEFAULT_MAX_CLTV_EXPIRY);
        }

        @Test
        void with_fee_rate_weight() {
            int feeRateWeight = 123;
            when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY_4);
            PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
            MultiPathPayment multiPathPayment =
                    multiPathPaymentSplitter.getMultiPathPaymentTo(PUBKEY_2, AMOUNT, paymentOptions, FINAL_CLTV_DELTA);
            assertThat(multiPathPayment.amount()).isEqualTo(Coins.NONE);
            verify(flowComputation)
                    .getOptimalFlows(PUBKEY_4, PUBKEY_2, AMOUNT, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY);
        }

        @Test
        void success() {
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(FLOW));
            when(grpcGetInfo.getPubkey()).thenReturn(PUBKEY);

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPaymentTo(
                    PUBKEY_2,
                    AMOUNT,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );

            Route expectedRoute = new Route(List.of(noInformationFor(EDGE)), AMOUNT);
            MultiPathPayment expected = new MultiPathPayment(List.of(expectedRoute));
            assertThat(multiPathPayment).isEqualTo(expected);
        }
    }

    @Nested
    class GetMultiPathPayment {

        private final Edge firstEdge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);

        @Test
        void failure() {
            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void one_flow_has_fee_rate_above_limit_including_fees_from_first_hop() {
            mockExtensionEdge(PUBKEY_4, 0);
            int feeRate = 200;
            Coins amount = Coins.ofSatoshis(1_000_000);
            Policy policy = policyFor(feeRate);
            PaymentOptions paymentOptions = PaymentOptions.forTopUp(feeRate - 1, 0, PUBKEY_2);
            mockFlow(amount, policy, paymentOptions);

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_4,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Nested
        class Expiry {
            private final Coins amount = Coins.ofSatoshis(1_000_000);
            private final Policy policy = policyFor(0);
            private final PaymentOptions paymentOptions = PaymentOptions.forTopUp(100, 0, PUBKEY_2);

            @BeforeEach
            void setUp() {
                // pubkey to pubkey2 has expiry delta of 40
                mockFlow(amount, policy, paymentOptions);
                mockExtensionEdge(PUBKEY_4, 0);
            }

            @Test
            void one_flow_has_cltv_expiry_at_limit() {
                MultiPathPayment multiPathPayment = getMultiPathPayment(DEFAULT_MAX_CLTV_EXPIRY - 40);
                assertThat(multiPathPayment.isFailure()).isFalse();
            }

            @Test
            void one_flow_has_cltv_expiry_above_limit() {
                MultiPathPayment multiPathPayment = getMultiPathPayment(DEFAULT_MAX_CLTV_EXPIRY - 40 + 1);
                assertThat(multiPathPayment.isFailure()).isTrue();
            }

            @Test
            void one_flow_has_cltv_expiry_above_limit_with_configured_max_cltv_expiry() {
                when(configurationService.getIntegerValue(MAX_CLTV_EXPIRY))
                        .thenReturn(Optional.of(40 + FINAL_CLTV_DELTA - 1));

                MultiPathPayment multiPathPayment = getMultiPathPayment(FINAL_CLTV_DELTA);
                assertThat(multiPathPayment.isFailure()).isTrue();
            }

            @Test
            void one_flow_has_cltv_expiry_at_limit_with_configured_max_cltv_expiry() {
                int limit = 40 + FINAL_CLTV_DELTA;
                when(configurationService.getIntegerValue(MAX_CLTV_EXPIRY)).thenReturn(Optional.of(limit));
                when(cltvService.getMaximumDeltaForEdges(anyInt(), anyInt(), any(), any())).thenReturn(limit);

                MultiPathPayment multiPathPayment = getMultiPathPayment(FINAL_CLTV_DELTA);
                assertThat(multiPathPayment.isFailure()).isFalse();
                verify(flowComputation).getOptimalFlows(any(), any(), any(), any(), eq(limit));
                verify(cltvService).getMaximumDeltaForEdges(limit, FINAL_CLTV_DELTA, Optional.of(PUBKEY_2), PUBKEY_4);
            }

            @Test
            void uses_maximum_delta_per_edge_from_cltv_service() {
                int expectedLimit = 2342;
                int maximumCltvExpiry = 5000;
                when(configurationService.getIntegerValue(MAX_CLTV_EXPIRY)).thenReturn(Optional.of(maximumCltvExpiry));
                when(cltvService.getMaximumDeltaForEdges(anyInt(), anyInt(), any(), any())).thenReturn(expectedLimit);
                getMultiPathPayment(FINAL_CLTV_DELTA);
                verify(flowComputation).getOptimalFlows(any(), any(), any(), any(), eq(expectedLimit));
                verify(cltvService).getMaximumDeltaForEdges(
                        maximumCltvExpiry,
                        FINAL_CLTV_DELTA,
                        Optional.of(PUBKEY_2),
                        PUBKEY_4
                );
            }

            private MultiPathPayment getMultiPathPayment(int finalCltvDelta) {
                return multiPathPaymentSplitter.getMultiPathPayment(
                        PUBKEY,
                        PUBKEY_4,
                        amount,
                        paymentOptions,
                        finalCltvDelta
                );
            }
        }

        @Test
        void one_flow_has_fee_rate_at_limit_including_fees_from_first_hop() {
            mockExtensionEdge(PUBKEY_3, 0);
            int feeRate = 200;
            Coins amount = Coins.ofSatoshis(2_000_000);
            Policy policy = policyFor(feeRate);
            PaymentOptions paymentOptions = PaymentOptions.forTopUp(feeRate, 0, PUBKEY_2);
            mockFlow(amount, policy, paymentOptions);

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_3,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isFalse();
        }

        @Test
        void extension_edge_is_too_expensive() {
            int feeRate = 200;
            mockExtensionEdge(PUBKEY_3, feeRate);
            Coins amount = Coins.ofSatoshis(2_000_000);
            Policy policy = policyFor(0);
            PaymentOptions paymentOptions = PaymentOptions.forTopUp(feeRate - 1, 0, PUBKEY_2);
            mockFlow(amount, policy, paymentOptions);

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_3,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void one_flow_has_fee_rate_above_limit_excluding_fees_from_first_hop() {
            int feeRate = 200;
            Coins amount = Coins.ofSatoshis(1_000_000);
            Policy policy = policyFor(feeRate);
            PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRate - 1);
            Flow firstEdgeFlow = new Flow(firstEdge, amount);
            Edge edge = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy);
            Flow flow = new Flow(edge, amount);
            addEdgeWithoutInformation(edge);
            when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_3, amount, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY))
                    .thenReturn(new Flows(firstEdgeFlow, flow));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_3,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void fee_rate_at_limit_excluding_fees_from_first_hop() {
            int feeRate = 200;
            Coins amount = Coins.ofSatoshis(3_000_000);
            Policy policy = policyFor(feeRate);
            PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRate);
            Flow firstEdgeFlow = new Flow(firstEdge, amount);
            Edge edge = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_4, CAPACITY, policy);
            Flow flow = new Flow(edge, amount);
            addEdgeWithoutInformation(edge);
            when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_4, amount, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY))
                    .thenReturn(new Flows(firstEdgeFlow, flow));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_4,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isFalse();
        }

        @Test
        void one_flow_has_fee_rate_above_limit_but_average_fee_rate_is_below_limit_including_fees_from_first_hop() {
            // The cheaper flows might fail while the more expensive one settles. This is not what we want, so we have
            // to disregard all flows.
            mockExtensionEdge(PUBKEY_4, 0);
            int feeRate = 200;
            Coins halfOfAmount = Coins.ofSatoshis(500_000);
            Coins amount = halfOfAmount.add(halfOfAmount);
            PaymentOptions paymentOptions = PaymentOptions.forTopUp(feeRate - 1, 0, PUBKEY_2);
            Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policyFor(0));
            Edge edge2 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policyFor(feeRate));
            Flow flow1 = new Flow(edge1, halfOfAmount);
            Flow flow2 = new Flow(edge2, halfOfAmount);
            addEdgeWithoutInformation(edge1);
            addEdgeWithoutInformation(edge2);
            when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_2, amount, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY))
                    .thenReturn(new Flows(flow1, flow2));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_4,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void one_flow_has_fee_rate_above_limit_but_average_fee_rate_is_below_limit_excluding_fees_from_first_hop() {
            int feeRate = 200;
            Coins halfOfAmount = Coins.ofSatoshis(500_000);
            Coins amount = halfOfAmount.add(halfOfAmount);
            PaymentOptions paymentOptions = PaymentOptions.forFeeRateLimit(feeRate - 1);
            Flow firstEdgeFlow = new Flow(firstEdge, amount);
            Edge edge1 = new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, CAPACITY, policyFor(feeRate));
            Edge edge2 = new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, CAPACITY, policyFor(0));
            Flow flow1 = new Flow(edge1, halfOfAmount);
            Flow flow2 = new Flow(edge2, halfOfAmount);
            addEdgeWithoutInformation(edge1);
            addEdgeWithoutInformation(edge2);
            when(flowComputation.getOptimalFlows(PUBKEY, PUBKEY_3, amount, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY))
                    .thenReturn(new Flows(firstEdgeFlow, flow1, flow2));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_3,
                    amount,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void one_flow_probability() {
            long capacitySat = EDGE.capacity().satoshis();
            Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
            Flow flow = new Flow(EDGE, halfOfCapacity);
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    halfOfCapacity,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(flow));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    halfOfCapacity,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );

            assertThat(multiPathPayment.probability())
                    .isEqualTo((1.0 * halfOfCapacity.satoshis() + 1) / (capacitySat + 1));
        }

        @Test
        void with_fee_rate_weight() {
            int feeRateWeight = 991;
            PaymentOptions paymentOptions = PaymentOptions.forFeeRateWeight(feeRateWeight);
            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.amount()).isEqualTo(Coins.NONE);
            verify(flowComputation).getOptimalFlows(PUBKEY, PUBKEY_2, AMOUNT, paymentOptions, DEFAULT_MAX_CLTV_EXPIRY);
        }

        @Test
        void two_flows_probability() {
            long capacitySat = CAPACITY.satoshis();
            Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
            Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);
            Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY_2, POLICY_1);
            Flow flow1 = new Flow(edge1, halfOfCapacity);
            Flow flow2 = new Flow(edge2, CAPACITY_2);
            addEdgeWithoutInformation(edge1);
            addEdgeWithoutInformation(edge2);
            Coins totalAmount = halfOfCapacity.add(CAPACITY_2);
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    totalAmount,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(flow1, flow2));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    totalAmount,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );

            double probabilityFlow1 = (1.0 * halfOfCapacity.satoshis() + 1) / (capacitySat + 1);
            double probabilityFlow2 = 1.0 / (CAPACITY_2.satoshis() + 1);
            assertThat(multiPathPayment.probability()).isEqualTo(probabilityFlow1 * probabilityFlow2);
        }

        @Test
        void two_flows_through_same_channel_probability() {
            long capacitySat = EDGE.capacity().satoshis();
            Coins halfOfCapacity = Coins.ofSatoshis(capacitySat / 2);
            Flow flow1 = new Flow(EDGE, halfOfCapacity);
            Flow flow2 = new Flow(EDGE, halfOfCapacity);
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    EDGE.capacity(),
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(flow1, flow2));

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    EDGE.capacity(),
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );

            assertThat(multiPathPayment.probability()).isEqualTo(1.0 / (capacitySat + 1));
        }

        @Test
        void adds_remainder_to_most_probable_route() {
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(FLOW));
            assumeThat(FLOW.amount()).isLessThan(AMOUNT);
            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.routes().iterator().next().getAmount()).isEqualTo(AMOUNT);
        }

        @Test
        void adds_hop_if_peer_is_specified_in_payment_options() {
            Edge extensionEdge = mockExtensionEdge(PUBKEY_3, 0);
            MultiPathPayment multiPathPayment = attemptTopUpPayment();
            assertThat(multiPathPayment.routes().iterator().next().getEdges()).contains(extensionEdge);
        }

        @Test
        void extension_fails_no_channel_with_peer() {
            when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of());
            MultiPathPayment multiPathPayment = attemptTopUpPayment();
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        @Test
        void extension_fails_unable_to_get_policy_from_peer() {
            when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
            when(policyService.getPolicyFrom(CHANNEL_ID, PUBKEY_2)).thenReturn(Optional.empty());
            MultiPathPayment multiPathPayment = attemptTopUpPayment();
            assertThat(multiPathPayment.isFailure()).isTrue();
        }

        private MultiPathPayment attemptTopUpPayment() {
            PaymentOptions paymentOptions = PaymentOptions.forTopUp(500, 123, PUBKEY_2);
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    AMOUNT,
                    paymentOptions,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(FLOW));
            return multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_3,
                    AMOUNT,
                    paymentOptions,
                    FINAL_CLTV_DELTA
            );
        }

        @Test
        void adds_liquidity_information_to_route() {
            Coins amount = FLOW.amount();
            Edge edge = FLOW.edge();
            EdgeWithLiquidityInformation withLiquidityInformation =
                    EdgeWithLiquidityInformation.forKnownLiquidity(edge, Coins.ofSatoshis(1));
            when(edgeComputation.getEdgeWithLiquidityInformation(edge)).thenReturn(withLiquidityInformation);
            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    amount,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(FLOW));
            Route expectedRoute = new Route(List.of(withLiquidityInformation), amount);

            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    amount,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );
            assertThat(multiPathPayment.routes()).containsExactly(expectedRoute);
        }

        @Test
        void adds_remainder_to_most_probable_route_due_to_liquidity_information() {
            Coins oneSat = Coins.ofSatoshis(1);
            Coins amount = Coins.ofSatoshis(3);
            Coins largeCapacity = Coins.ofSatoshis(10);
            Coins smallCapacity = Coins.ofSatoshis(5);

            Edge edgeLargeCapacity = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, largeCapacity, POLICY_1);
            Edge edgeSmallCapacity = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, smallCapacity, POLICY_1);

            EdgeWithLiquidityInformation liquidityInformationLarge =
                    EdgeWithLiquidityInformation.forUpperBound(edgeLargeCapacity, Coins.ofSatoshis(2));
            EdgeWithLiquidityInformation liquidityInformationSmall = noInformationFor(edgeSmallCapacity);
            when(edgeComputation.getEdgeWithLiquidityInformation(edgeLargeCapacity))
                    .thenReturn(liquidityInformationLarge);
            when(edgeComputation.getEdgeWithLiquidityInformation(edgeSmallCapacity))
                    .thenReturn(liquidityInformationSmall);

            when(flowComputation.getOptimalFlows(
                    PUBKEY,
                    PUBKEY_2,
                    amount,
                    DEFAULT_PAYMENT_OPTIONS,
                    DEFAULT_MAX_CLTV_EXPIRY
            )).thenReturn(new Flows(
                    new Flow(edgeLargeCapacity, oneSat),
                    new Flow(edgeSmallCapacity, oneSat)
            ));
            assumeThat(FLOW.amount()).isLessThan(amount);
            MultiPathPayment multiPathPayment = multiPathPaymentSplitter.getMultiPathPayment(
                    PUBKEY,
                    PUBKEY_2,
                    amount,
                    DEFAULT_PAYMENT_OPTIONS,
                    FINAL_CLTV_DELTA
            );
            Route route1 = new Route(List.of(liquidityInformationLarge), oneSat);
            Route route2 = new Route(List.of(liquidityInformationSmall), amount.subtract(oneSat));
            assertThat(multiPathPayment.routes()).containsExactlyInAnyOrder(route1, route2);
        }

        private void mockFlow(Coins amount, Policy policy, PaymentOptions paymentOptions) {
            Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy);
            Flow flow = new Flow(edge, amount);
            addEdgeWithoutInformation(edge);
            Flows value = new Flows(flow);
            when(flowComputation.getOptimalFlows(
                    eq(PUBKEY),
                    eq(PUBKEY_2),
                    eq(amount),
                    eq(paymentOptions),
                    anyInt())
            ).thenReturn(value);
        }

        private void addEdgeWithoutInformation(Edge edge) {
            when(edgeComputation.getEdgeWithLiquidityInformation(edge)).thenReturn(noInformationFor(edge));
        }
    }

    private Edge mockExtensionEdge(Pubkey destination, int feeRate) {
        Policy policyExtension =
                new Policy(feeRate, Coins.NONE, true, 40, Coins.ofSatoshis(10_000));
        when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicyFrom(CHANNEL_ID, PUBKEY_2)).thenReturn(Optional.of(policyExtension));
        Edge extensionEdge =
                new Edge(CHANNEL_ID, PUBKEY_2, destination, LOCAL_OPEN_CHANNEL.getCapacity(), policyExtension);
        when(edgeComputation.getEdgeWithLiquidityInformation(extensionEdge))
                .thenReturn(noInformationFor(extensionEdge));
        return extensionEdge;
    }

    private static Policy policyFor(int feeRate) {
        return new Policy(feeRate, Coins.NONE, true, 40, Coins.ofSatoshis(10_000));
    }

    private EdgeWithLiquidityInformation noInformationFor(Edge edgeSmallCapacity) {
        return EdgeWithLiquidityInformation.forUpperBound(edgeSmallCapacity, edgeSmallCapacity.capacity());
    }
}
