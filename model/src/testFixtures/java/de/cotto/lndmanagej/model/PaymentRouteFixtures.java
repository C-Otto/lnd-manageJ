package de.cotto.lndmanagej.model;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_1_LAST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_2_LAST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_3_FIRST;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4_FIRST;

public class PaymentRouteFixtures {
    public static final PaymentRoute PAYMENT_ROUTE_4_TO_2 =
            new PaymentRoute(PAYMENT_HOP_CHANNEL_4_FIRST, PAYMENT_HOP_CHANNEL_2_LAST);
    public static final PaymentRoute PAYMENT_ROUTE_4_TO_1 =
            new PaymentRoute(PAYMENT_HOP_CHANNEL_4_FIRST, PAYMENT_HOP_CHANNEL_1_LAST);
    public static final PaymentRoute PAYMENT_ROUTE_3_TO_1 =
            new PaymentRoute(PAYMENT_HOP_CHANNEL_3_FIRST, PAYMENT_HOP_CHANNEL_1_LAST);
    public static final PaymentRoute PAYMENT_ROUTE_3_TO_2 =
            new PaymentRoute(PAYMENT_HOP_CHANNEL_3_FIRST, PAYMENT_HOP_CHANNEL_2_LAST);
}
