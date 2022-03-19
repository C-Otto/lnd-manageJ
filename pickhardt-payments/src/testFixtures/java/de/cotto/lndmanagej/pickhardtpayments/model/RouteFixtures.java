package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.List;

import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;

public class RouteFixtures {
    public static final Route ROUTE = new Route(List.of(EDGE), Coins.ofSatoshis(100));
}
