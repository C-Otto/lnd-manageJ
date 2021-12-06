package de.cotto.lndmanagej.model;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_2;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_3;

public class PaymentRouteFixtures {
    public static final PaymentRoute PAYMENT_ROUTE =
            new PaymentRoute(List.of(PAYMENT_HOP, PAYMENT_HOP_2, PAYMENT_HOP_3));
    public static final PaymentRoute PAYMENT_ROUTE_2 =
            new PaymentRoute(List.of(PAYMENT_HOP, PAYMENT_HOP_2));
}
