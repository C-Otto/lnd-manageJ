package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.invoices.persistence.SettledInvoiceJpaDto;
import de.cotto.lndmanagej.model.SelfPayment;
import de.cotto.lndmanagej.payments.persistence.PaymentJpaDto;

public record SelfPaymentJpaDto(
        PaymentJpaDto payment,
        SettledInvoiceJpaDto settledInvoice
) {
    public SelfPayment toModel() {
        return new SelfPayment(payment.toModel(), settledInvoice.toModel());
    }
}
