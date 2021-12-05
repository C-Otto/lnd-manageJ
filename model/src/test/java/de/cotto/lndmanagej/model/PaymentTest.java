package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_CREATION_DATE_TIME;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_FEES;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_HASH;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_VALUE;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentTest {
    @Test
    void index() {
        assertThat(PAYMENT.index()).isEqualTo(PAYMENT_INDEX);
    }

    @Test
    void paymentHash() {
        assertThat(PAYMENT.paymentHash()).isEqualTo(PAYMENT_HASH);
    }

    @Test
    void creationDateTime() {
        assertThat(PAYMENT.creationDateTime()).isEqualTo(PAYMENT_CREATION_DATE_TIME);
    }

    @Test
    void value() {
        assertThat(PAYMENT.value()).isEqualTo(PAYMENT_VALUE);
    }

    @Test
    void fees() {
        assertThat(PAYMENT.fees()).isEqualTo(PAYMENT_FEES);
    }
}