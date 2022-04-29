package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_3_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.BasicRouteFixtures.BASIC_ROUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class BasicRouteTest {

    @Test
    void zero_amount() {
        assertThatIllegalArgumentException().isThrownBy(() -> new BasicRoute(List.of(), Coins.NONE));
    }

    @Test
    void negative_amount() {
        assertThatIllegalArgumentException().isThrownBy(() -> new BasicRoute(List.of(), Coins.ofSatoshis(-1)));
    }

    @Test
    void amount() {
        assertThat(BASIC_ROUTE.amount()).isEqualTo(Coins.ofSatoshis(100));
    }

    @Test
    void edges() {
        assertThat(BASIC_ROUTE.edges()).containsExactly(EDGE, EDGE_2_3, EDGE_3_4);
    }
}
