package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RouteTest {
    @Test
    void getProbability() {
        long capacitySat = EDGE.capacity().satoshis();
        assertThat(ROUTE.getProbability())
                .isEqualTo(1.0 * (capacitySat + 1 - ROUTE.amount().satoshis()) / (capacitySat + 1));
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
