package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentJpaDtoTest {
    private SelfPaymentJpaDto selfPaymentJpaDto;

    @BeforeEach
    void setUp() {
        SettledInvoiceJpaDto invoice = SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE);
        PaymentJpaDto payment = PaymentJpaDto.createFromModel(PAYMENT);
        selfPaymentJpaDto = new SelfPaymentJpaDto(payment, invoice);
    }

    @Test
    void toModel() {
        assertThat(selfPaymentJpaDto.toModel()).isEqualTo(SELF_PAYMENT);
    }

    @Test
    void payment() {
        assertThat(selfPaymentJpaDto.payment().toModel()).isEqualTo(PAYMENT);
    }

    @Test
    void settledInvoice() {
        assertThat(selfPaymentJpaDto.settledInvoice().toModel()).isEqualTo(SETTLED_INVOICE);
    }
}