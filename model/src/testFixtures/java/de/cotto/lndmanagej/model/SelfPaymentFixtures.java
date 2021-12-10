package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;

public class SelfPaymentFixtures {
    public static final SelfPayment SELF_PAYMENT = new SelfPayment(PAYMENT, SETTLED_INVOICE);
    public static final SelfPayment SELF_PAYMENT_2 = new SelfPayment(PAYMENT_2, SETTLED_INVOICE_2);
}
