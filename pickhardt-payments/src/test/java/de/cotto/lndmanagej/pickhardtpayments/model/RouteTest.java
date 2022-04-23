package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RouteTest {

    private static final int ONE_MILLION = 1_000_000;
    private static final int TIME_LOCK_DELTA = 40;
    private static final int BLOCK_HEIGHT = 700_000;

    @Test
    void getProbability() {
        assertThat(ROUTE.getProbability()).isEqualTo(0.999_985_714_354_421_7);
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
        int ppm1 = 50;
        int ppm2 = 100;
        Coins baseFee1 = Coins.ofMilliSatoshis(15);
        Coins baseFee2 = Coins.ofMilliSatoshis(10);
        Coins expectedFees =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm2 / ONE_MILLION))
                        .add(baseFee2);
        Policy policy1 = new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA);
        Edge hop1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1);
        Policy policy2 = new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA);
        Edge hop2 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy2);
        assertThat(new Route(List.of(hop1, hop2), amount).fees())
                .isEqualTo(expectedFees);
    }

    @Test
    void fees_one_hop() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee = Coins.ofMilliSatoshis(10);
        int ppm = 100;
        assertThat(new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm, baseFee, true, TIME_LOCK_DELTA))
        ), amount).fees()).isEqualTo(Coins.NONE);
    }

    @Test
    void fees_two_hops() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(10);
        Coins baseFee2 = Coins.ofMilliSatoshis(5);
        int ppm1 = 100;
        int ppm2 = 200;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm2 / ONE_MILLION))
                        .add(baseFee2);
        Coins expectedFees1 = Coins.NONE;
        Coins expectedFees = expectedFees1.add(expectedFees2);
        Route route = new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA))
        ), amount);
        assertThat(route.fees()).isEqualTo(expectedFees);
    }

    @Test
    void fees_three_hops() {
        Coins amount = Coins.ofSatoshis(3_000_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(100);
        Coins baseFee2 = Coins.ofMilliSatoshis(50);
        Coins baseFee3 = Coins.ofMilliSatoshis(10);
        int ppm1 = 100;
        int ppm2 = 200;
        int ppm3 = 300;
        Coins expectedFees3 = Coins.NONE;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm3 / ONE_MILLION))
                        .add(baseFee3);
        long amountWithFeesLastHop = amount.add(expectedFees2).milliSatoshis();
        Coins expectedFees1 = Coins.ofMilliSatoshis(
                (long) (amountWithFeesLastHop * 1.0 * ppm2 / ONE_MILLION)
        ).add(baseFee2);
        Coins expectedFees = expectedFees1.add(expectedFees2).add(expectedFees3);
        assertThat(new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID_3, PUBKEY_3, PUBKEY_4, CAPACITY, new Policy(ppm3, baseFee3, true, TIME_LOCK_DELTA))
        ), amount).fees()).isEqualTo(expectedFees);
    }

    @Test
    void feesWithFirstHop_empty() {
        assertThat(new Route(List.of(), Coins.ofSatoshis(1_500_000)).feesWithFirstHop()).isEqualTo(Coins.NONE);
    }

    @Test
    void feesWithFirstHop_one_hop() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee = Coins.ofMilliSatoshis(10);
        int ppm = 100;
        Coins expectedFees = Coins.ofMilliSatoshis(amount.milliSatoshis() * ppm / ONE_MILLION).add(baseFee);
        assertThat(new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm, baseFee, true, TIME_LOCK_DELTA))
        ), amount).feesWithFirstHop()).isEqualTo(expectedFees);
    }

    @Test
    void feesWithFirstHop_three_hops() {
        Coins amount = Coins.ofSatoshis(1_500_000);
        Coins baseFee1 = Coins.ofMilliSatoshis(10);
        Coins baseFee2 = Coins.ofMilliSatoshis(5);
        Coins baseFee3 = Coins.ofMilliSatoshis(1);
        int ppm1 = 100;
        int ppm2 = 200;
        int ppm3 = 300;
        Coins feesForThirdHop = Coins.ofMilliSatoshis(amount.milliSatoshis() * ppm3 / ONE_MILLION).add(baseFee3);
        Coins feesForSecondHop =
                Coins.ofMilliSatoshis(amount.add(feesForThirdHop).milliSatoshis() * ppm2 / ONE_MILLION).add(baseFee2);
        Coins amountForFirstHop = amount.add(feesForThirdHop).add(feesForSecondHop);
        Coins feesForFirstHop =
                Coins.ofMilliSatoshis(amountForFirstHop.milliSatoshis() * ppm1 / ONE_MILLION).add(baseFee1);
        Coins expectedFees = feesForFirstHop.add(feesForSecondHop).add(feesForThirdHop);
        assertThat(new Route(List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm1, baseFee1, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm2, baseFee2, true, TIME_LOCK_DELTA)),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, new Policy(ppm3, baseFee3, true, TIME_LOCK_DELTA))
        ), amount).feesWithFirstHop()).isEqualTo(expectedFees);
    }

    @Test
    void feeForHop() {
        Coins amount = Coins.ofSatoshis(2_000);
        int ppm1 = 123;
        int ppm2 = 456;
        int ppm3 = 789;
        Coins expectedFees3 = Coins.NONE;
        Coins expectedFees2 =
                Coins.ofMilliSatoshis((long) (amount.milliSatoshis() * 1.0 * ppm3 / ONE_MILLION));
        Coins expectedFees1 =
                Coins.ofMilliSatoshis((long) (amount.add(expectedFees2).milliSatoshis() * 1.0 * ppm2 / ONE_MILLION));
        Route route = createRoute(amount, ppm1, ppm2, ppm3);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.feeForHop(2)).isEqualTo(expectedFees3);
        softly.assertThat(route.feeForHop(1)).isEqualTo(expectedFees2);
        softly.assertThat(route.feeForHop(0)).isEqualTo(expectedFees1);
        softly.assertAll();
    }

    @Test
    void forwardAmountForHop() {
        Coins amount = Coins.ofSatoshis(2_000);
        Route route = createRoute(amount, 123, 456, 789);
        Coins feeForHop2 = route.feeForHop(1);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.forwardAmountForHop(2)).isEqualTo(amount);
        softly.assertThat(route.forwardAmountForHop(1)).isEqualTo(amount);
        softly.assertThat(route.forwardAmountForHop(0)).isEqualTo(amount.add(feeForHop2));
        softly.assertAll();
    }

    @Test
    void expiryForHop_route_with_one_hop() {
        int timeLockDelta = 123;
        int finalCltvDelta = 456;
        Policy policy = new Policy(100, Coins.NONE, true, timeLockDelta);
        List<Edge> edges = List.of(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy));
        Route route = new Route(edges, Coins.ofSatoshis(1));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertAll();
    }

    @Test
    void expiry_route_with_two_hops() {
        int timeLockDelta1 = 40;
        int timeLockDelta2 = 123;
        int finalCltvDelta = 456;
        Policy policy1 = new Policy(100, Coins.NONE, true, timeLockDelta1);
        Policy policy2 = new Policy(100, Coins.NONE, true, timeLockDelta2);
        List<Edge> edges = List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy2)
        );
        Route route = new Route(edges, Coins.ofSatoshis(100));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.expiryForHop(1, BLOCK_HEIGHT, finalCltvDelta)).isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta1);
        softly.assertAll();
    }

    @Test
    void expiry_route_with_three_hops() {
        int timeLockDelta1 = 40;
        int timeLockDelta2 = 123;
        int timeLockDelta3 = 9;
        int finalCltvDelta = 456;
        Policy policy1 = new Policy(100, Coins.NONE, true, timeLockDelta1);
        Policy policy2 = new Policy(100, Coins.NONE, true, timeLockDelta2);
        Policy policy3 = new Policy(100, Coins.NONE, true, timeLockDelta3);
        List<Edge> edges = List.of(
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy2),
                new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy3)
        );
        Route route = new Route(edges, Coins.ofSatoshis(1));
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(route.expiryForHop(0, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta2);
        softly.assertThat(route.expiryForHop(1, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.expiryForHop(2, BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta);
        softly.assertThat(route.totalTimeLock(BLOCK_HEIGHT, finalCltvDelta))
                .isEqualTo(BLOCK_HEIGHT + finalCltvDelta + timeLockDelta2 + timeLockDelta1);
        softly.assertAll();
    }

    @Test
    void feeRate_two_hops_without_base_fee() {
        int feeRate1 = 100;
        int feeRate2 = 987;
        Coins amount = Coins.ofSatoshis(1_234_000);
        Route route = createRoute(amount, feeRate1, feeRate2);
        assertThat(route.getFeeRate()).isEqualTo(feeRate2);
    }

    @Test
    void feeRate_one_hop_with_base_fee() {
        int feeRate1 = 100;
        int feeRate2 = 987;
        Policy policy1 = new Policy(feeRate1, Coins.ofSatoshis(100_000), true, TIME_LOCK_DELTA);
        Policy policy2 = new Policy(feeRate2, Coins.ofSatoshis(10_000), true, TIME_LOCK_DELTA);
        Coins amount = Coins.ofSatoshis(1_234_567);
        Edge hop1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy1);
        Edge hop2 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY, policy2);
        assertThat(new Route(List.of(hop1, hop2), amount).getFeeRate())
                .isEqualTo(9087);
    }

    @Test
    void feeRate_three_hops() {
        int feeRate1 = 50;
        int feeRate2 = 100;
        int feeRate3 = 350;
        Coins amount = Coins.ofSatoshis(1_234_567);
        assertThat(createRoute(amount, feeRate1, feeRate2, feeRate3).getFeeRate())
                .isEqualTo(feeRate2 + feeRate3);
    }

    @Test
    void feeRateWithFirstHop_three_hops() {
        int feeRate1 = 50;
        int feeRate2 = 100;
        int feeRate3 = 350;
        Coins amount = Coins.ofSatoshis(1_234_567);
        assertThat(createRoute(amount, feeRate1, feeRate2, feeRate3).getFeeRateWithFirstHop())
                .isEqualTo(feeRate1 + feeRate2 + feeRate3);
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
        List<Coins> updatedFeesForHops = List.of(Coins.ofMilliSatoshis(200), Coins.ofMilliSatoshis(200), Coins.NONE);
        assertThat(original.getForAmount(newAmount))
                .isEqualTo(new Route(original.edges(), newAmount, updatedFeesForHops, original.liquidityInformation()));
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

    private Route createRoute(Coins amount, int... feeRates) {
        List<Edge> edges = Arrays.stream(feeRates)
                .mapToObj(ppm -> new Policy(ppm, Coins.NONE, true, TIME_LOCK_DELTA))
                .map(policy -> new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, policy))
                .toList();
        return new Route(edges, amount);
    }
}
