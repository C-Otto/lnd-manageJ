package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_2_LAST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_FIRST;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_4_TO_2;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteTest {
    @Test
    void firstHop() {
        assertThat(PAYMENT_ROUTE_4_TO_2.firstHop()).contains(PAYMENT_HOP_CHANNEL_4_FIRST);
    }

    @Test
    void lastHop() {
        assertThat(PAYMENT_ROUTE_4_TO_2.lastHop()).contains(PAYMENT_HOP_CHANNEL_2_LAST);
    }

}
