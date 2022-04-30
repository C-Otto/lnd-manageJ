package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.List;

import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE;
import static de.cotto.lndmanagej.model.RouteFixtures.ROUTE_2;

public class MultiPathPaymentFixtures {
    public static final MultiPathPayment MULTI_PATH_PAYMENT = new MultiPathPayment(List.of(ROUTE, ROUTE_2));
}
