package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_2;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_3;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteTest {
    @Test
    void hops() {
        assertThat(PAYMENT_ROUTE.hops()).isEqualTo(List.of(PAYMENT_HOP, PAYMENT_HOP_2, PAYMENT_HOP_3));
    }

}