package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
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
    void feeRate_failure() {
        assertThat(MultiPathPayment.FAILURE.getFeeRate()).isZero();
    }

    @Test
    void fees_failure() {
        assertThat(MultiPathPayment.FAILURE.fees()).isEqualTo(Coins.NONE);
    }

    @Test
    void amount() {
        assertThat(MULTI_PATH_PAYMENT.amount()).isEqualTo(ROUTE.amount());
    }

    @Test
    void routes() {
        assertThat(MULTI_PATH_PAYMENT.routes()).containsExactly(ROUTE);
    }

    @Test
    void fees() {
        assertThat(MULTI_PATH_PAYMENT.fees()).isEqualTo(Coins.ofMilliSatoshis(20));
    }

    @Test
    void feeRate() {
        assertThat(MULTI_PATH_PAYMENT.getFeeRate()).isEqualTo(200);
    }
}
