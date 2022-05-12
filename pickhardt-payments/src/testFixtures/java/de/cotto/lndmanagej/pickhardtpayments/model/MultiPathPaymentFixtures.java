package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.List;

import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_3;

public class MultiPathPaymentFixtures {
    public static final MultiPathPayment MULTI_PATH_PAYMENT = new MultiPathPayment(List.of(ROUTE, ROUTE_2));
    public static final MultiPathPayment MULTI_PATH_PAYMENT_2 = new MultiPathPayment(List.of(ROUTE_3));
}
