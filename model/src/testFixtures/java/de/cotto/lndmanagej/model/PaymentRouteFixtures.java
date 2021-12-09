package de.cotto.lndmanagej.model;

import java.util.List;

import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_1;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_2;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_3;
import static de.cotto.lndmanagej.model.PaymentHopFixtures.PAYMENT_HOP_CHANNEL_4;

public class PaymentRouteFixtures {
    public static final PaymentRoute PAYMENT_ROUTE_4_TO_2 =
            new PaymentRoute(List.of(PAYMENT_HOP_CHANNEL_4, PAYMENT_HOP_CHANNEL_3, PAYMENT_HOP_CHANNEL_2));
    public static final PaymentRoute PAYMENT_ROUTE_4_TO_1 =
            new PaymentRoute(List.of(PAYMENT_HOP_CHANNEL_4, PAYMENT_HOP_CHANNEL_3, PAYMENT_HOP_CHANNEL_1));
    public static final PaymentRoute PAYMENT_ROUTE_3_TO_1 =
            new PaymentRoute(List.of(PAYMENT_HOP_CHANNEL_3, PAYMENT_HOP_CHANNEL_1));
}
