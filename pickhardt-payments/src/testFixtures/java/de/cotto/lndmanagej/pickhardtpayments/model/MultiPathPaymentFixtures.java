package de.cotto.lndmanagej.pickhardtpayments.model;

import java.util.Set;

import static de.cotto.lndmanagej.pickhardtpayments.model.RouteFixtures.ROUTE;

public class MultiPathPaymentFixtures {
    public static final MultiPathPayment MULTI_PATH_PAYMENT =
            new MultiPathPayment(ROUTE.amount(), ROUTE.getProbability(), Set.of(ROUTE));
}
