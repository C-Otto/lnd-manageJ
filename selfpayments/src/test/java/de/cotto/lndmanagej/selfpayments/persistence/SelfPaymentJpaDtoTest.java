package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.model.Payment;
import de.cotto.lndmanagej.model.PaymentRoute;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_CREATION;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_FEES;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_HASH_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_INDEX_4;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_VALUE;
import static de.cotto.lndmanagej.model.SelfPaymentFixtures.SELF_PAYMENT_4;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_4;
import static org.assertj.core.api.Assertions.assertThat;

class SelfPaymentJpaDtoTest {
    private SelfPaymentJpaDto selfPaymentJpaDto;

    @BeforeEach
    void setUp() {
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_4),
                PaymentJpaDto.createFromModel(PAYMENT_4)
        );
    }

    @Test
    void toModel() {
        assertThat(selfPaymentJpaDto.toModel()).isEqualTo(SELF_PAYMENT_4);
    }

    @Test
    void toModel_no_route() {
        Payment payment = new Payment(
                PAYMENT_INDEX_4, PAYMENT_HASH_4, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, List.of()
        );
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_4),
                PaymentJpaDto.createFromModel(payment)
        );
        assertThat(selfPaymentJpaDto.toModel().routes()).isEmpty();
    }

    @Test
    void toModel_empty_route() {
        List<PaymentRoute> routes = List.of(new PaymentRoute(Optional.empty(), Optional.empty()));
        Payment payment = new Payment(
                PAYMENT_INDEX_4, PAYMENT_HASH_4, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, routes
        );
        selfPaymentJpaDto = new SelfPaymentJpaDto(
                SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_4),
                PaymentJpaDto.createFromModel(payment)
        );
        assertThat(selfPaymentJpaDto.toModel().routes()).isEmpty();
    }

    @Test
    void invoice() {
        assertThat(selfPaymentJpaDto.invoice()).usingRecursiveComparison()
                .isEqualTo(SettledInvoiceJpaDto.createFromModel(SETTLED_INVOICE_4));
    }

    @Test
    void payment() {
        assertThat(selfPaymentJpaDto.payment()).usingRecursiveComparison()
                .isEqualTo(PaymentJpaDto.createFromModel(PAYMENT_4));
    }
}
