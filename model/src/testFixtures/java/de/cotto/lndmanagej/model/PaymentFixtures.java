package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.util.List;

import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_3_TO_1;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_3_TO_2;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_4_TO_1;
import static de.cotto.lndmanagej.model.PaymentRouteFixtures.PAYMENT_ROUTE_4_TO_2;

public class PaymentFixtures {
    public static final int PAYMENT_INDEX = 2;
    public static final int PAYMENT_INDEX_2 = 3;
    public static final int PAYMENT_INDEX_3 = 4;
    public static final int PAYMENT_INDEX_4 = 5;
    public static final String PAYMENT_HASH = "1234";
    public static final String PAYMENT_HASH_2 = "aaa0";
    public static final String PAYMENT_HASH_3 = "aaa1";
    public static final String PAYMENT_HASH_4 = "aaa2";
    public static final Coins PAYMENT_VALUE = Coins.ofSatoshis(1_000_000);
    public static final Coins PAYMENT_FEES = Coins.ofMilliSatoshis(10);
    public static final List<PaymentRoute> ONE_ROUTE_4_TO_2 = List.of(PAYMENT_ROUTE_4_TO_2);
    public static final List<PaymentRoute> TWO_ROUTES = List.of(PAYMENT_ROUTE_4_TO_1, PAYMENT_ROUTE_3_TO_1);
    public static final List<PaymentRoute> TWO_PARALLEL_ROUTES = List.of(PAYMENT_ROUTE_4_TO_1, PAYMENT_ROUTE_3_TO_2);
    public static final LocalDateTime PAYMENT_CREATION =
            LocalDateTime.of(2021, 12, 5, 22, 22, 22, 500_000_000);

    public static final Payment PAYMENT = new Payment(
            PAYMENT_INDEX, PAYMENT_HASH, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, ONE_ROUTE_4_TO_2
    );

    public static final Payment PAYMENT_2 = new Payment(
            PAYMENT_INDEX_2, PAYMENT_HASH_2, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, TWO_ROUTES
    );

    public static final Payment PAYMENT_3 = new Payment(
            PAYMENT_INDEX_3, PAYMENT_HASH_3, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, TWO_ROUTES
    );

    public static final Payment PAYMENT_4 = new Payment(
            PAYMENT_INDEX_4, PAYMENT_HASH_4, PAYMENT_CREATION, PAYMENT_VALUE, PAYMENT_FEES, TWO_PARALLEL_ROUTES
    );
}
