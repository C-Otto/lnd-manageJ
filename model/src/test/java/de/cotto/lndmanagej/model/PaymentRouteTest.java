package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_2;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_3;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_4_TO_2;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteTest {
    @Test
    void hops() {
        assertThat(PAYMENT_ROUTE_4_TO_2.hops()).isEqualTo(
                List.of(PAYMENT_HOP_CHANNEL_4, PAYMENT_HOP_CHANNEL_3, PAYMENT_HOP_CHANNEL_2)
        );
    }

}