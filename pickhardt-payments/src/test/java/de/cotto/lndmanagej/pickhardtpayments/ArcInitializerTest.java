package de.cotto.lndmanagej.pickhardtpayments;

import com.google.ortools.Loader;
import com.google.ortools.graph.MinCostFlow;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.EdgeWithLiquidityInformation;
import de.cotto.lndmanagej.pickhardtpayments.model.IntegerMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_3_4;
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
    private final Map<Integer, Edge> edgeMapping = new LinkedHashMap<>();
    private final ArcInitializer arcInitializer = new ArcInitializer(
            minCostFlow,
            integerMapping,
            edgeMapping,
            QUANTIZATION,
            PIECEWISE_LINEAR_APPROXIMATIONS,
            FEE_RATE_WEIGHT
    );

    @Test
    void no_edge() {
        arcInitializer.addArcs(Set.of());
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void ignores_edge_with_zero_capacity() {
        arcInitializer.addArcs(Set.of(edge(EDGE, Coins.NONE)));
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void edge_with_capacity_equal_to_quantization_amount() {
        int quantization = 10_000;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                FEE_RATE_WEIGHT
        );
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(quantization));
        arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
        assertThat(minCostFlow.getNumArcs()).isOne();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void edge_with_known_liquidity_is_added_as_arc_without_cost() {
        Coins capacity = Coins.ofSatoshis(100);
        Coins knownLiquidity = Coins.ofSatoshis(25);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(EDGE.withCapacity(capacity), knownLiquidity);

        arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));

        assertThat(minCostFlow.getNumArcs()).isEqualTo(1);
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
        assertThat(minCostFlow.getCapacity(0)).isEqualTo(25);
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
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
            assertThat(minCostFlow.getCapacity(0)).isEqualTo(25);
        }

        @Test
        void adds_uncertain_liquidity_as_second_arc() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    QUANTIZATION,
                    2,
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
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
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
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
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(0);
        }

        @Test
        void known_amount_rounded_due_to_quantization() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    20,
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
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
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
            // one arc for the known liquidity (25 / 10 = 2), 100 / 10 - 2 = 8 remaining
            assertThat(minCostFlow.getCapacity(1)).isEqualTo(8);
        }

        @Test
        void does_not_add_arcs_without_capacity() {
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    20,
                    6,
                    FEE_RATE_WEIGHT
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
            // one arc for the known liquidity (25 / 20 = 1), 100 / 20 - 1 = 4 remaining: 4 < 5, no additional arc added
            assertThat(minCostFlow.getNumArcs()).isEqualTo(1);
        }

        @Test
        void adds_fee_rate_as_cost() {
            int feeRateWeight = 1;
            ArcInitializer arcInitializer = new ArcInitializer(
                    minCostFlow,
                    integerMapping,
                    edgeMapping,
                    QUANTIZATION,
                    PIECEWISE_LINEAR_APPROXIMATIONS,
                    feeRateWeight
            );
            arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
            assertThat(minCostFlow.getUnitCost(0)).isEqualTo(200);
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
                FEE_RATE_WEIGHT
        );
        arcInitializer.addArcs(List.of(
                edge(EDGE, Coins.ofSatoshis(100)),
                edge(EDGE_2_3, Coins.ofSatoshis(200))
        ));
        assertThat(edgeMapping.get(0)).isEqualTo(EDGE.withCapacity(Coins.ofSatoshis(100)));
        assertThat(edgeMapping.get(piecesPerChannel)).isEqualTo(EDGE_2_3.withCapacity(Coins.ofSatoshis(200)));
    }

    @Test
    void ignores_edge_with_capacity_smaller_than_quantization_amount() {
        int quantization = 10_000;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                FEE_RATE_WEIGHT
        );
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                edge(EDGE, Coins.ofSatoshis(quantization - 1));
        arcInitializer.addArcs(Set.of(edgeWithLiquidityInformation));
        assertThat(minCostFlow.getNumArcs()).isZero();
    }

    @Test
    void uses_quantization_for_capacity() {
        int quantization = 100;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                FEE_RATE_WEIGHT
        );
        arcInitializer.addArcs(Set.of(edge(EDGE, Coins.ofSatoshis(20_123))));
        assertThat(minCostFlow.getCapacity(0)).isEqualTo(201);
    }

    @Test
    void uses_quantization_for_unit_cost() {
        int quantization = 100;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                quantization,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                FEE_RATE_WEIGHT
        );
        arcInitializer.addArcs(List.of(
                edge(EDGE, Coins.ofSatoshis(20_123)),
                edge(EDGE_3_4, Coins.ofSatoshis(1_000_000))
        ));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 50_000_000L / 201);
    }

    @Test
    void one_edge() {
        arcInitializer.addArcs(Set.of(edge(EDGE, Coins.ofSatoshis(1))));
        assertThat(minCostFlow.getNumArcs()).isOne();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void edges_with_piecewise_linear_approximation() {
        int piecewiseLinearApproximations = 5;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                piecewiseLinearApproximations,
                FEE_RATE_WEIGHT
        );
        arcInitializer.addArcs(List.of(
                edge(EDGE, Coins.ofSatoshis(10_000)),
                edge(EDGE_2_3, Coins.ofSatoshis(30_000))
        ));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(10);
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(5_000_000L);
        assertThat(minCostFlow.getCapacity(0)).isEqualTo(2_000);
        assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10_000_000L);
        assertThat(minCostFlow.getCapacity(1)).isEqualTo(2_000);
        assertThat(minCostFlow.getUnitCost(2)).isEqualTo(15_000_000L);
        assertThat(minCostFlow.getCapacity(2)).isEqualTo(2_000);
        assertThat(minCostFlow.getUnitCost(3)).isEqualTo(20_000_000L);
        assertThat(minCostFlow.getCapacity(3)).isEqualTo(2_000);
        assertThat(minCostFlow.getUnitCost(4)).isEqualTo(25_000_000L);
        assertThat(minCostFlow.getCapacity(4)).isEqualTo(2_000);
    }

    @Test
    void two_edges() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(1));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(2));
        arcInitializer.addArcs(Set.of(edge1, edge2));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(2);
    }

    @Test
    void parallel_edges_are_not_combined() {
        EdgeWithLiquidityInformation edge = edge(EDGE, Coins.ofSatoshis(1));
        arcInitializer.addArcs(List.of(edge, edge));
        assertThat(minCostFlow.getNumArcs()).isEqualTo(2);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity_above_assumed_maximum() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(3_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(21_000_000_000L));
        arcInitializer.addArcs(List.of(edge1, edge2));
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
        arcInitializer.addArcs(List.of(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 6_000 / 3);
        assertThat(minCostFlow.getUnitCost(1)).isEqualTo(10 * 6_000 / 9);
    }

    @Test
    void unit_cost_is_rounded_down() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(3_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(10_000_000_000L));
        arcInitializer.addArcs(List.of(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 10_000 / 3);
    }

    @Test
    void computes_unit_cost_based_on_assumed_maximum_capacity() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(4_999_999_999L));
        arcInitializer.addArcs(List.of(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000_000_000L / 2_000_000_000);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(5_000_000_001L));
        arcInitializer.addArcs(List.of(edge1, edge2));
        assertThat(minCostFlow.getUnitCost(0)).isEqualTo(10 * 5_000_000_001L / 2_000_000_000);
    }

    @Test
    void computes_unit_cost_based_on_maximum_capacity_above_assumed_maximum_without_combining_parallel_edges() {
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(2_000_000));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(6_000_000_000L));
        EdgeWithLiquidityInformation edge3 = edge(EDGE_1_3, Coins.ofSatoshis(7_000_000_000L));
        arcInitializer.addArcs(List.of(edge1, edge2, edge3));
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

    private EdgeWithLiquidityInformation setupWithTwoEdges(long capacitySmaller, long capacityLarger) {
        int feeRateWeight = 1;
        ArcInitializer arcInitializer = new ArcInitializer(
                minCostFlow,
                integerMapping,
                edgeMapping,
                QUANTIZATION,
                PIECEWISE_LINEAR_APPROXIMATIONS,
                feeRateWeight
        );
        EdgeWithLiquidityInformation edge1 = edge(EDGE, Coins.ofSatoshis(capacitySmaller));
        EdgeWithLiquidityInformation edge2 = edge(EDGE_1_3, Coins.ofSatoshis(capacityLarger));
        arcInitializer.addArcs(List.of(edge1, edge2));
        return edge1;
    }

    private EdgeWithLiquidityInformation edge(Edge edge, Coins capacity) {
        Edge edgeWithCapacity = edge.withCapacity(capacity);
        return EdgeWithLiquidityInformation.forUpperBound(edgeWithCapacity, capacity);
    }
}
