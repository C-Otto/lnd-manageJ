package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Policy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
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
}
