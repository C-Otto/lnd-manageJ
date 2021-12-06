package de.cotto.lndmanagej.payments.persistence;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_2;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_3;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentRouteJpaDtoTest {
    @Test
    void toModel() {
        assertThat(new PaymentRouteJpaDto(
                List.of(
                        PaymentHopJpaDto.createFromModel(PAYMENT_HOP),
                        PaymentHopJpaDto.createFromModel(PAYMENT_HOP_2),
                        PaymentHopJpaDto.createFromModel(PAYMENT_HOP_3)
                )
        ).toModel()).isEqualTo(PAYMENT_ROUTE);
    }

    @Test
    void createFromModel() {
        assertThat(PaymentRouteJpaDto.createFromModel(PAYMENT_ROUTE).toModel()).isEqualTo(PAYMENT_ROUTE);
    }
}