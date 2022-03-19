package de.cotto.lndmanagej.pickhardtpayments.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPathPaymentTest {
    @Test
    void probability_failure() {
        assertThat(MultiPathPayment.FAILURE.probability()).isZero();
    }

    @Test
    void amount() {
        assertThat(MULTI_PATH_PAYMENT.amount()).isEqualTo(ROUTE.amount());
    }

    @Test
    void routes() {
        assertThat(MULTI_PATH_PAYMENT.routes()).containsExactly(ROUTE);
    }
}
