package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.List;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_3_4;

public class RouteFixtures {
    public static final Route ROUTE = new Route(List.of(EDGE, EDGE_2_3, EDGE_3_4), Coins.ofSatoshis(100));
    public static final Route ROUTE_2 = new Route(List.of(EDGE_1_3, EDGE_2_3), Coins.ofSatoshis(200));
}
