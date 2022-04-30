package de.cotto.lndmanagej.model;

import java.util.List;

import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE_3_4;

public class BasicRouteFixtures {
    public static final BasicRoute BASIC_ROUTE =
            new BasicRoute(List.of(EDGE, EDGE_2_3, EDGE_3_4), Coins.ofSatoshis(100));
    public static final BasicRoute BASIC_ROUTE_2 =
            new BasicRoute(List.of(EDGE_1_3, EDGE_2_3), Coins.ofSatoshis(200));
}
