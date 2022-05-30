package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_2;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_3;
import static de.cotto.lndmanagej.model.PaymentFixtures.PAYMENT_4;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_2;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_3;
import static de.cotto.lndmanagej.model.SettledInvoiceFixtures.SETTLED_INVOICE_4;

public class SelfPaymentFixtures {
    public static final SelfPayment SELF_PAYMENT = new SelfPayment(PAYMENT, SETTLED_INVOICE);
    public static final SelfPayment SELF_PAYMENT_2 = new SelfPayment(PAYMENT_2, SETTLED_INVOICE_2);
    public static final SelfPayment SELF_PAYMENT_3 = new SelfPayment(PAYMENT_3, SETTLED_INVOICE_3);
    public static final SelfPayment SELF_PAYMENT_4 = new SelfPayment(PAYMENT_4, SETTLED_INVOICE_4);
}
