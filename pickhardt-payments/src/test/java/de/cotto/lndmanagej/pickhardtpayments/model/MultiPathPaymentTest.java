package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPaymentFixtures.MULTI_PATH_PAYMENT;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE_2;
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
        assertThat(MULTI_PATH_PAYMENT.amount()).isEqualTo(ROUTE.amount().add(ROUTE_2.amount()));
    }

    @Test
    void routes() {
        assertThat(MULTI_PATH_PAYMENT.routes()).containsExactlyInAnyOrder(ROUTE, ROUTE_2);
    }

    @Test
    void fees() {
        assertThat(MULTI_PATH_PAYMENT.fees()).isEqualTo(ROUTE.fees().add(ROUTE_2.fees()));
    }

    @Test
    void feesWithFirstHop() {
        assertThat(MULTI_PATH_PAYMENT.feesWithFirstHop())
                .isEqualTo(ROUTE.feesWithFirstHop().add(ROUTE_2.feesWithFirstHop()));
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
