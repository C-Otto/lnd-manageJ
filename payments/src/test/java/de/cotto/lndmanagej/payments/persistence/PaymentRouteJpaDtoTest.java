package de.cotto.lndmanagej.payments.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_2_LAST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_FIRST;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_4_TO_2;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteJpaDtoTest {
    @Test
    void toModel() {
        assertThat(new PaymentRouteJpaDto(
                List.of(
                        PaymentHopJpaDto.createFromModel(PAYMENT_HOP_CHANNEL_4_FIRST),
                        PaymentHopJpaDto.createFromModel(PAYMENT_HOP_CHANNEL_2_LAST)
                )
        ).toModel()).isEqualTo(PAYMENT_ROUTE_4_TO_2);
    }

    @Test
    void createFromModel() {
        assertThat(PaymentRouteJpaDto.createFromModel(PAYMENT_ROUTE_4_TO_2).toModel()).isEqualTo(PAYMENT_ROUTE_4_TO_2);
    }
}
