package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.Loader;
import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgesWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_3_4;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class ArcInitializerTest {
    private static final int QUANTIZATION = 1;
    private static final int PIECEWISE_LINEAR_APPROXIMATIONS = 1;
    private static final int FEE_RATE_WEIGHT = 0;

    static {
        Loader.loadNativeLibraries();
    }

    private final MinCostFlow minCostFlow = new MinCostFlow();
    private final IntegerMapping<Pubkey> integerMapping = new IntegerMapping<>();
    private final IntObjectHashMap<Edge> edgeMapping = new IntObjectHashMap<>();
    private final ArcInitializer arcInitializer = new ArcInitializer(
            minCostFlow,
            integerMapping,
            edgeMapping,
            QUANTIZATION,
            PIECEWISE_LINEAR_APPROXIMATIONS,
            FEE_RATE_WEIGHT,
            PUBKEY,
            true
    );

    @Test
    void no_edge() {
        arcInitializer.addArcs(EdgesWithLiquidityInformation.EMPTY);
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void ignores_edge_with_zero_capacity() {
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge(EDGE, Coins.NONE)));
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void edge_with_capacity_equal_to_quantization_amount() {
        int quantization = 10_000;
        ArcInitializer arcInitializer = getArcInitializerWithQuantization(quantization);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(quantization));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
        // upper bound is reduced by one unit
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void edge_with_capacity_just_above_quantization_amount() {
        int quantization = 10_000;
        int capacity = 10_100;
        ArcInitializer arcInitializer = getArcInitializerWithQuantization(quantization);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(capacity));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
        // upper bound is reduced by one unit, and remainder is rounded down
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void edge_with_capacity_more_than_quantization_amount() {
        int quantization = 5_000;
        int capacity = 15_000;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                2,
                FEE_RATE_WEIGHT,
                PUBKEY,
                true
        );
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(capacity));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(2);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(1);
        softly.assertThat(minCostFlow.getCapacity(1)).isEqualTo(1);
        softly.assertAll();
    }

    @Test
    void edge_with_low_capacity() {
        long quantization = 10_000;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                5,
                FEE_RATE_WEIGHT,
                PUBKEY,
                true
        );
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(40_000));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(3);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(1);
        softly.assertThat(minCostFlow.getCapacity(1)).isEqualTo(1);
        softly.assertThat(minCostFlow.getCapacity(2)).isEqualTo(1);
        softly.assertAll();
    }

    @Nested
    class EdgeWithLowerAndUpperBound {

        private EdgeWithLiquidityInformation edgeWithLiquidityInformation;

        @BeforeEach
        void setUp() {
            Coins capacity = Coins.ofSatoshis(100);
            Coins knownLiquidity = Coins.ofSatoshis(25);
            edgeWithLiquidityInformation = EdgeWithLiquidityInformation.forLowerAndUpperBound(
                    EDGE.withCapacity(capacity),
                    knownLiquidity,
                    capacity
            );
        }

        @Test
        void added_as_arc_without_cost() {
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getNumArcs()).isEqualTo(1);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
            softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(24);
            softly.assertAll();
        }

        @Test
        void adds_uncertain_liquidity_as_second_arc() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    QUANTIZATION,
                    2,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10 * 5_000_000_000L / (100 - 25));
            assertThat(minCostFlow.getCapacity(1)).isEqualTo(75);
        }

        @Test
        void splits_uncertain_liquidity_as_additional_arcs() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    QUANTIZATION,
                    5,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getNumArcs()).isEqualTo(5);
        }

        @Test
        void known_amount_matches_quantization() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    edgeWithLiquidityInformation.availableLiquidityLowerBound().satoshis(),
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            // lower bound is reduced by one unit, so there is no edge with unit cost 0
            assertThat(minCostFlow.getUnitCost(0)).isPositive();
        }

        @Test
        void known_amount_just_a_bit_more_than_quantization() {
            long quantization = 1_000;
            int lowerBound = 1_999;
            EdgeWithLiquidityInformation edge =
                    EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(lowerBound));
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    quantization,
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
            assertThat(minCostFlow.getNumArcs()).isZero();
        }

        @Test
        void known_amount_more_than_quantization() {
            long quantization = 1_000;
            int lowerBound = 2_123;
            EdgeWithLiquidityInformation edge =
                    EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(lowerBound));
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    quantization,
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(minCostFlow.getNumArcs()).isOne();
            softly.assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
            softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(1);
            softly.assertAll();
        }

        @Test
        void known_amount_reduced_and_rounded_due_to_quantization() {
            ArcInitializer arcInitializer = getArcInitializerWithQuantization(10);
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getCapacity(0)).isEqualTo(1);
        }

        @Test
        void splits_remaining_liquidity_after_rounding_known_liquidity() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    10,
                    2,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            // lower bound 25, reduced by one quantization unit: 15
            // upper bound 100, reduced by one quantization unit: 90
            // one arc for the adjusted lower bound: 15 / 10 = 1
            // second arc for remaining liquidity: 90 / 10 - 1 = 8
            assertThat(minCostFlow.getCapacity(1)).isEqualTo(8);
        }

        @Test
        void adds_arcs_for_unknown_capacity_even_if_amount_is_small() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    12,
                    5,
                    FEE_RATE_WEIGHT,
                    PUBKEY,
                    true
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            // lower bound 25, reduced by one quantization unit: 13
            // upper bound 100, reduced by one quantization unit: 88
            // one arc for the known liquidity, capacity: 13 / 12 = 1
            // remaining capacity: 88 / 12 - 1 = 6
            // four additional arcs with capacity 1, unused capacity: 6 - 4*1 = 2
            assertThat(minCostFlow.getNumArcs()).isEqualTo(5);
            SoftAssertions softly = new SoftAssertions();
            softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(1);
            softly.assertThat(minCostFlow.getUnitCost(0)).isZero();
            softly.assertThat(minCostFlow.getCapacity(1)).isEqualTo(1);
            softly.assertThat(minCostFlow.getCapacity(2)).isEqualTo(1);
            softly.assertThat(minCostFlow.getCapacity(3)).isEqualTo(1);
            softly.assertThat(minCostFlow.getCapacity(4)).isEqualTo(1);
            softly.assertAll();
        }

        @Test
        void adds_fee_rate_as_cost() {
            int feeRateWeight = 1;
            ArcInitializer arcInitializer = getArcInitializer(QUANTIZATION, feeRateWeight);
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(200);
        }

        @Test
        void includes_base_fee_in_assumed_fee_rate() {
            Coins baseFee = Coins.ofMilliSatoshis(100);

            int feeRateWeight = 1;
            int quantization = 10_000;
            ArcInitializer arcInitializer = getArcInitializer(quantization, feeRateWeight);
            addEdgeWithBaseFee(baseFee, quantization, arcInitializer);

            long expectedFeeRate = (long) Math.ceil(200 + 10);
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(expectedFeeRate);
        }

        @Test
        void includes_base_fee_in_assumed_fee_rate_rounds_up() {
            Coins baseFee = Coins.ofMilliSatoshis(111);

            int feeRateWeight = 1;
            int quantization = 10_000;
            ArcInitializer arcInitializer = getArcInitializer(quantization, feeRateWeight);
            addEdgeWithBaseFee(baseFee, quantization, arcInitializer);

            long expectedFeeRate = (long) Math.ceil(200 + 12);
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(expectedFeeRate);
        }

        private void addEdgeWithBaseFee(Coins baseFee, int quantization, ArcInitializer arcInitializer) {
            Policy policy = new Policy(200, baseFee, true, 40, Coins.ofSatoshis(10_000));
            EdgeWithLiquidityInformation edge = EdgeWithLiquidityInformation.forKnownLiquidity(
                    new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy),
                    Coins.ofSatoshis(30L * quantization)
            );
            arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
        }

        private ArcInitializer getArcInitializer(int quantization, int feeRateWeight) {
            return new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    quantization,
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    feeRateWeight,
                    PUBKEY_2,
                    true
            );
        }
    }

    @Test
    void adds_edge_to_edgeMapping() {
        int piecesPerChannel = 2;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                piecesPerChannel,
                FEE_RATE_WEIGHT,
                PUBKEY,
                true
        );
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(
                edge(EDGE, Coins.ofSatoshis(100)),
                edge(EDGE_2_3, Coins.ofSatoshis(200))
        ));
        assertThat(edgeMapping.get(0)).isEqualTo(EDGE.withCapacity(Coins.ofSatoshis(100)));
        assertThat(edgeMapping.get(piecesPerChannel)).isEqualTo(EDGE_2_3.withCapacity(Coins.ofSatoshis(200)));
    }

    @Test
    void adds_arcs_for_edge_with_capacity_smaller_than_quantization_amount() {
        int quantization = 10_000;
        ArcInitializer arcInitializer = getArcInitializerWithQuantization(quantization);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(quantization - 1));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edgeWithLiquidityInformation));
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void uses_quantization_for_capacity() {
        int quantization = 100;
        ArcInitializer arcInitializer = getArcInitializerWithQuantization(quantization);
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge(EDGE, Coins.ofSatoshis(20_223))));
        assertThat(minCostFlow.getCapacity(0)).isEqualTo(201);
    }

    @Test
    void uses_quantization_for_unit_cost() {
        int quantization = 100;
        ArcInitializer arcInitializer = getArcInitializerWithQuantization(quantization);
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(
                edge(EDGE, Coins.ofSatoshis(20_223)),
                edge(EDGE_3_4, Coins.ofSatoshis(1_000_000))
        ));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 50_000_000L / 201);
    }

    @Test
    void ignores_edge_with_capacity_matching_quantization() {
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge(EDGE, Coins.ofSatoshis(1))));
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void one_edge() {
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge(EDGE, Coins.ofSatoshis(2))));
        assertThat(minCostFlow.getNumArcs()).isOne();
    }

    @Test
    void edges_with_piecewise_linear_approximation() {
        int piecewiseLinearApproximations = 5;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                piecewiseLinearApproximations,
                FEE_RATE_WEIGHT,
                PUBKEY,
                true
        );
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(
                edge(EDGE, Coins.ofSatoshis(10_001)),
                edge(EDGE_2_3, Coins.ofSatoshis(30_001))
        ));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(10);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(minCostFlow.getUnitCost(0)).isEqualTo(5_000_000L);
        softly.assertThat(minCostFlow.getCapacity(0)).isEqualTo(2_000);
        softly.assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10_000_000L);
        softly.assertThat(minCostFlow.getCapacity(1)).isEqualTo(2_000);
        softly.assertThat(minCostFlow.getUnitCost(2)).isEqualTo(15_000_000L);
        softly.assertThat(minCostFlow.getCapacity(2)).isEqualTo(2_000);
        softly.assertThat(minCostFlow.getUnitCost(3)).isEqualTo(20_000_000L);
        softly.assertThat(minCostFlow.getCapacity(3)).isEqualTo(2_000);
        softly.assertThat(minCostFlow.getUnitCost(4)).isEqualTo(25_000_000L);
        softly.assertThat(minCostFlow.getCapacity(4)).isEqualTo(2_000);
        softly.assertAll();
    }

    @Test
    void two_edges() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(3));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(2);
    }

    @Test
    void parallel_edges_are_not_combined() {
        EdgeWithLiquidityInformation edge = edge(EDGE, Coins.ofSatoshis(2));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge, edge));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(2);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity_above_assumed_maximum() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(3_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(21_000_000_000L));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 21_000 / 3);
        assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity_even_if_upper_bound_is_lower() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(3_000_000));
        EdgeWithLiquidityInformation edge2 = EdgeWithLiquidityInformation.forUpperBound(
                EDGE_1_3.withCapacity(Coins.ofSatoshis(6_000_000_000L)),
                Coins.ofSatoshis(9_000_000)
        );
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 6_000 / 3);
        assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10 * 6_000 / 9);
    }

    @Test
    void unit_cost_is_rounded_down() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(3_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(10_000_000_000L));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 10_000 / 3);
    }

    @Test
    void computes_unit_cost_based_on_assumed_maximum_capacity() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(4_999_999_999L));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000_000_000L / 2_000_000_000);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(5_000_000_001L));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000_000_001L / 2_000_000_000);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity_above_assumed_maximum_without_combining_parallel_edges() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(6_000_000_000L));
        EdgeWithLiquidityInformation edge3 = edge(EDGE_1_3, Coins.ofSatoshis(7_000_000_000L));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2, edge3));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 7_000 / 2);
    }

    @Test
    void computes_unit_cost_with_fee_rate_weight() {
        EdgeWithLiquidityInformation edge1 = setupWithTwoEdges(2_000_000, 4_000_000);
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000 / 2 + edge1.edge().policy().feeRate());
    }

    @Test
    void unit_cost_with_fee_rate_weight_is_not_affected_by_size_of_largest_channel_below_assumed_maximum() {
        // same as above, but with larger second channel
        EdgeWithLiquidityInformation edge1 = setupWithTwoEdges(3_000_000, 5_000_000);
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000 / 3 + edge1.edge().policy().feeRate());
    }

    @Test
    void unit_cost_with_fee_rate_weight_is_affected_by_size_of_largest_channel_above_assumed_maximum() {
        // same as above, but with much larger second channel
        EdgeWithLiquidityInformation edge1 = setupWithTwoEdges(4_000_000, 7_000_000_000L);
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 7_000 / 4 + edge1.edge().policy().feeRate());
    }

    @Test
    void uses_zero_fee_rate_for_edge_from_own_node_with_unknown_liquidity() {
        // same as above, but with much larger second channel
        int feeRateWeight = 1;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                feeRateWeight,
                PUBKEY,
                true
        );
        EdgeWithLiquidityInformation edge = edge(EDGE, Coins.ofSatoshis(4_000_000));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000 / 4);
    }

    @Test
    void uses_actual_fee_rate_for_edge_from_own_node_with_unknown_liquidity_if_configured() {
        ArcInitializer arcInitializer = getArcInitializerConsideringFeesForLocalChannel();
        EdgeWithLiquidityInformation edge = edge(EDGE, Coins.ofSatoshis(6_000_000));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000 / 6 + 200);
    }

    @Test
    void uses_zero_fee_rate_for_edge_from_own_node_with_known_liquidity() {
        // same as above, but with much larger second channel
        int feeRateWeight = 1;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                feeRateWeight,
                PUBKEY,
                true
        );
        Coins capacity = Coins.ofSatoshis(4_000_000);
        Edge edgeWithCapacity = EDGE.withCapacity(capacity);
        EdgeWithLiquidityInformation edge =
                EdgeWithLiquidityInformation.forKnownLiquidity(edgeWithCapacity, Coins.ofSatoshis(2_000_000));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
    }

    @Test
    void uses_actual_fee_rate_for_edge_from_own_node_with_known_liquidity_if_configured() {
        ArcInitializer arcInitializer = getArcInitializerConsideringFeesForLocalChannel();
        Coins capacity = Coins.ofSatoshis(5_000_000);
        Edge edgeWithCapacity = EDGE.withCapacity(capacity);
        EdgeWithLiquidityInformation edge =
                EdgeWithLiquidityInformation.forKnownLiquidity(edgeWithCapacity, Coins.ofSatoshis(2_000_000));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(200);
    }

    private ArcInitializer getArcInitializerConsideringFeesForLocalChannel() {
        return new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                1,
                PUBKEY,
                false
        );
    }

    private EdgeWithLiquidityInformation setupWithTwoEdges(long capacitySmaller, long capacityLarger) {
        int feeRateWeight = 1;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                feeRateWeight,
                PUBKEY_2,
                true
        );
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(capacitySmaller));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(capacityLarger));
        arcInitializer.addArcs(new EdgesWithLiquidityInformation(edge1, edge2));
        return edge1;
    }

    private EdgeWithLiquidityInformation edge(Edge edge, Coins capacity) {
        Edge edgeWithCapacity = edge.withCapacity(capacity);
        return EdgeWithLiquidityInformation.forUpperBound(edgeWithCapacity, capacity);
    }

    private ArcInitializer getArcInitializerWithQuantization(int quantization) {
        return new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                FEE_RATE_WEIGHT,
                PUBKEY,
                true
        );
    }
}
