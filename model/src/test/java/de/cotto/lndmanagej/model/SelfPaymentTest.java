package de.cotto.lndmanagej.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_CREATION;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_FEES;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_HASH_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_VALUE;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_4;
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
    void amountPaid() {
        assertThat(SELF_PAYMENT_2.amountPaid()).isEqualTo(SETTLED_INVOICE_2.amountPaid());
    }

    @Test
    void fees() {
        assertThat(SELF_PAYMENT_2.fees()).isEqualTo(PAYMENT_2.fees());
    }

    @Test
    void routes() {
        SelfPayment selfPayment = new SelfPayment(PAYMENT_4, SETTLED_INVOICE_4);
        assertThat(selfPayment.routes()).containsExactly(
                new SelfPaymentRoute(CHANNEL_ID_4, Coins.ofSatoshis(1), CHANNEL_ID),
                new SelfPaymentRoute(CHANNEL_ID_3, Coins.ofSatoshis(2), CHANNEL_ID_2)
        );
    }

    @Test
    void routes_empty() {
        Payment payment = new Payment(
                PAYMENT_INDEX_4, PAYMENT_HASH_4, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, List.of()
        );
        SelfPayment selfPayment = new SelfPayment(payment, SETTLED_INVOICE_4);
        assertThat(selfPayment.routes()).isEmpty();
    }
}
