package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static org.assertj.core.api.Assertions.assertThat;

class MultiPathPaymentTest {
    @Test
    void isFailure() {
        assertThat(MultiPathPayment.FAILURE.isFailure()).isTrue();
    }

    @Test
    void isFailure_false() {
        assertThat(MULTI_PATH_PAYMENT.isFailure()).isFalse();
    }

    @Test
    void probability_failure() {
        assertThat(MultiPathPayment.FAILURE.probability()).isZero();
    }

    @Test
    void feeRate_failure() {
        assertThat(MultiPathPayment.FAILURE.getFeeRate()).isZero();
    }

    @Test
    void feeRateWithFirstHop_failure() {
        assertThat(MultiPathPayment.FAILURE.getFeeRateWithFirstHop()).isZero();
    }

    @Test
    void fees_failure() {
        assertThat(MultiPathPayment.FAILURE.fees()).isEqualTo(Coins.NONE);
    }

    @Test
    void feesWithFirstHop_failure() {
        assertThat(MultiPathPayment.FAILURE.feesWithFirstHop()).isEqualTo(Coins.NONE);
    }

    @Test
    void amount() {
        assertThat(MULTI_PATH_PAYMENT.amount()).isEqualTo(ROUTE.getAmount().add(ROUTE_2.getAmount()));
    }

    @Test
    void routes() {
        assertThat(MULTI_PATH_PAYMENT.routes()).containsExactlyInAnyOrder(ROUTE, ROUTE_2);
    }

    @Test
    void fees() {
        assertThat(MULTI_PATH_PAYMENT.fees()).isEqualTo(ROUTE.getFees().add(ROUTE_2.getFees()));
    }

    @Test
    void feesWithFirstHop() {
        assertThat(MULTI_PATH_PAYMENT.feesWithFirstHop())
                .isEqualTo(ROUTE.getFeesWithFirstHop().add(ROUTE_2.getFeesWithFirstHop()));
    }

    @Test
    void feeRate() {
        assertThat(MULTI_PATH_PAYMENT.getFeeRate()).isEqualTo(266);
    }

    @Test
    void feeRateWithFirstHop() {
        assertThat(MULTI_PATH_PAYMENT.getFeeRateWithFirstHop()).isEqualTo(466);
    }
}
