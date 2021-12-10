package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentTest {
    @Test
    void memo() {
        assertThat(SELF_PAYMENT.memo()).isEqualTo(SETTLED_INVOICE.memo());
    }

    @Test
    void settleDate() {
        assertThat(SELF_PAYMENT_2.settleDate()).isEqualTo(SETTLED_INVOICE_2.settleDate());
    }

    @Test
    void value() {
        assertThat(SELF_PAYMENT_2.value()).isEqualTo(PAYMENT_2.value());
    }

    @Test
    void fees() {
        assertThat(SELF_PAYMENT_2.fees()).isEqualTo(PAYMENT_2.fees());
    }

    @Test
    void firstChannel() {
        assertThat(SELF_PAYMENT_2.firstChannel()).isEqualTo(PAYMENT_2.getFirstChannel());
    }

    @Test
    void lastChannel() {
        assertThat(SELF_PAYMENT_2.lastChannel()).isEqualTo(SETTLED_INVOICE_2.receivedVia());
    }
}