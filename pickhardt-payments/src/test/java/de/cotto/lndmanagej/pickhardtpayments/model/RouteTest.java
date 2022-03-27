package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RouteTest {

    private static final int ONE_MILLION = 1_000_000;

    @Test
    void getProbability() {
        long capacitySat = EDGE.capacity().satoshis();
        assertThat(ROUTE.getProbability())
                .isEqualTo(1.0 * (capacitySat + 1 - ROUTE.amount().satoshis()) / (capacitySat + 1));
    }

    @Test
    void getProbability_within_known_liquidity() {
        long availableLiquiditySat = 100;
        Coins capacity = Coins.ofSatoshis(200);
        Coins amount = Coins.ofSatoshis(90);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        Route route = new Route(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(edge, Coins.ofSatoshis(availableLiquiditySat));
        assertThat(route.withLiquidityInformation(Set.of(edgeWithLiquidityInformation)).getProbability())
                .isEqualTo(1.0);
    }

    @Test
    void getProbability_exactly_known_liquidity() {
        Route route = routeForAmountAndCapacityAndKnownLiquidity(100, 200, 100);
        assertThat(route.getProbability())
                .isEqualTo(1.0);
    }

    @Test
    void getProbability_above_known_liquidity() {
        Route route = routeForAmountAndCapacityAndKnownLiquidity(250, 300, 200);
        assertThat(route.getProbability()).isEqualTo(0.0);
    }

    @Test
    void getProbability_above_known_lower_bound_for_liquidity() {
        long lowerBoundSat = 100;
        long capacitySat = 200;
        int amountSat = 150;
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, Coins.ofSatoshis(capacitySat), POLICY_1);
        Route route = new Route(List.of(edge), Coins.ofSatoshis(amountSat));
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forLowerBound(edge, Coins.ofSatoshis(lowerBoundSat));
        assertThat(route.withLiquidityInformation(Set.of(edgeWithLiquidityInformation)).getProbability())
                .isEqualTo(1.0 * (capacitySat + 1 - amountSat) / (capacitySat + 1 - lowerBoundSat));
    }

    @Test
    void getProbability_below_known_upper_bound_for_liquidity() {
        long upperBoundSat = 100;
        Coins capacity = Coins.ofSatoshis(200);
        Coins amount = Coins.ofSatoshis(80);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        Route route = new Route(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forUpperBound(edge, Coins.ofSatoshis(upperBoundSat));
        assertThat(route.withLiquidityInformation(Set.of(edgeWithLiquidityInformation)).getProbability())
                .isEqualTo(1.0 * (upperBoundSat + 1 - amount.satoshis()) / (upperBoundSat + 1));
    }

    @Test
    void fees_amount_with_milli_sat() {
        Coins amount = Coins.ofMilliSatoshis(1_500_000_111);
        int ppm = 100;
        Coins baseFee = Coins.ofMilliSatoshis(10);
        Coins expectedFees =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm / ONE_MILLION))
                        .add(baseFee);
        Policy policy = new Policy(ppm, baseFee, true);
        assertThat(new Route(List.of(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy)), amount).fees())
                .isEqualTo(expectedFees);
    }

    @Test
    void fees_two_hops() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(10);
        Coins baseFee2 = Coins.ofMilliSatoshis(1);
        int ppm1 = 100;
        int ppm2 = 200;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm2 / ONE_MILLION))
                        .add(baseFee2);
        Coins expectedFees1 =
                Coins.ofMilliSatoshis((long) (amount.add(expectedFees2).milliSatoshis() * 1.0 * ppm1 / ONE_MILLION))
                        .add(baseFee1);
        Coins expectedFees = expectedFees1.add(expectedFees2);
        assertThat(new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true)),
                new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, new Policy(ppm2, baseFee2, true))
        ), amount).fees()).isEqualTo(expectedFees);
    }

    @Test
    void feeRate_one_hop_without_base_fee() {
        int feeRate = 987;
        Policy policy = new Policy(feeRate, Coins.ofMilliSatoshis(0), true);
        Coins amount = Coins.ofSatoshis(1_234_000);
        assertThat(new Route(List.of(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy)), amount).getFeeRate())
                .isEqualTo(feeRate);
    }

    @Test
    void feeRate_one_hop_with_base_fee() {
        int feeRate = 987;
        Policy policy = new Policy(feeRate, Coins.ofSatoshis(10_000), true);
        Coins amount = Coins.ofSatoshis(1_234_567);
        assertThat(new Route(List.of(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy)), amount).getFeeRate())
                .isEqualTo(9087);
    }

    @Test
    void feeRate_two_hops() {
        int feeRate1 = 100;
        int feeRate2 = 350;
        Policy policy1 = new Policy(feeRate1, Coins.ofMilliSatoshis(0), true);
        Policy policy2 = new Policy(feeRate2, Coins.ofMilliSatoshis(0), true);
        Coins amount = Coins.ofSatoshis(1_234_567);
        Edge hop1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1);
        Edge hop2 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy2);
        assertThat(new Route(List.of(hop1, hop2), amount).getFeeRate()).isEqualTo(feeRate1 + feeRate2);
    }

    @Test
    void zero_amount() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Route(List.of(), Coins.NONE));
    }

    @Test
    void negative_amount() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Route(List.of(), Coins.ofSatoshis(-1)));
    }

    @Test
    void getRouteForAmount() {
        Coins newAmount = Coins.ofSatoshis(1_000);
        assertThat(ROUTE.getForAmount(newAmount)).isEqualTo(new Route(ROUTE.edges(), newAmount));
    }

    @Test
    void getRouteForAmount_retains_liquidity_information() {
        Route original = ROUTE.withLiquidityInformation(Set.of(
                EdgeWithLiquidityInformation.forUpperBound(EDGE, Coins.ofSatoshis(123))
        ));
        Coins newAmount = Coins.ofSatoshis(1_000);
        Coins updateFees = Coins.ofMilliSatoshis(110);
        assertThat(original.getForAmount(newAmount))
                .isEqualTo(new Route(original.edges(), newAmount, updateFees, original.liquidityInformation()));
    }

    @Test
    void liquidityInformation_default() {
        assertThat(new Route(List.of(EDGE), Coins.ofSatoshis(1)).liquidityInformation()).isEmpty();
    }

    @Test
    void withLiquidityInformation() {
        Route route = new Route(List.of(EDGE), Coins.ofSatoshis(4));
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(EDGE, Coins.ofSatoshis(2));
        Set<EdgeWithLiquidityInformation> providedLiquidityInformation = Set.of(edgeWithLiquidityInformation);
        assertThat(route.withLiquidityInformation(providedLiquidityInformation).liquidityInformation())
                .containsExactly(entry(EDGE, edgeWithLiquidityInformation));
    }

    private Route routeForAmountAndCapacityAndKnownLiquidity(int amountSat, int capacitySat, int knownLiquiditySat) {
        Coins capacity = Coins.ofSatoshis(capacitySat);
        Coins amount = Coins.ofSatoshis(amountSat);
        Edge edge = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, capacity, POLICY_1);
        Route route = new Route(List.of(edge), amount);
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                EdgeWithLiquidityInformation.forKnownLiquidity(edge, Coins.ofSatoshis(knownLiquiditySat));
        return route.withLiquidityInformation(Set.of(edgeWithLiquidityInformation));
    }
}
