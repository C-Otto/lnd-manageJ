package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.util.List;

import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_2;

public class PaymentFixtures {
    public static final int PAYMENT_INDEX = 2;
    public static final int PAYMENT_INDEX_2 = 3;
    public static final String PAYMENT_HASH = "abc";
    public static final Coins PAYMENT_VALUE = Coins.ofSatoshis(1_000_000);
    public static final Coins PAYMENT_FEES = Coins.ofMilliSatoshis(10);
    private static final List<PaymentRoute> ONE_ROUTE = List.of(PAYMENT_ROUTE);
    private static final List<PaymentRoute> TWO_ROUTES = List.of(PAYMENT_ROUTE, PAYMENT_ROUTE_2);
    public static final LocalDateTime PAYMENT_CREATION_DATE_TIME =
            LocalDateTime.of(2021, 12, 5, 22, 22, 22, 500_000_000);

    public static final Payment PAYMENT = new Payment(
            PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, ONE_ROUTE
    );

    public static final Payment PAYMENT_2 = new Payment(
            PAYMENT_INDEX_2, PAYMENT_HASH, PAYMENT_CREATION_DATE_TIME, PAYMENT_VALUE, PAYMENT_FEES, TWO_ROUTES
    );
}
