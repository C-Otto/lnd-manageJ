package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.BasicRoute;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Route;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;

class RoutesTest {
    @Test
    void getFixedWithTotalAmount_adds_to_only_route() {
        BasicRoute basicRoute = new BasicRoute(List.of(EDGE), Coins.ofSatoshis(1));
        List<Route> routes = List.of(new Route(basicRoute));
        List<Route> fixedRoutes = Routes.getFixedWithTotalAmount(routes, Coins.ofSatoshis(2));
        BasicRoute expectedBasicRoute = new BasicRoute(List.of(EDGE), Coins.ofSatoshis(2));
        Route expectedRoute = new Route(expectedBasicRoute);
        assertThat(fixedRoutes).containsExactly(expectedRoute);
    }

    @Test
    void getFixedWithTotalAmount_adds_to_route_with_highest_probability() {
        Edge edge1 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);
        Edge edge2 = new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);
        Edge edge3 = new Edge(CHANNEL_ID_3, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);

        List<BasicRoute> basicRoutes = List.of(
                new BasicRoute(List.of(edge1), Coins.ofSatoshis(2)),
                new BasicRoute(List.of(edge2), Coins.ofSatoshis(1)),
                new BasicRoute(List.of(edge3), Coins.ofSatoshis(3))
        );
        List<Route> routes = basicRoutes.stream().map(Route::new).toList();

        List<Route> fixedRoutes = Routes.getFixedWithTotalAmount(routes, Coins.ofSatoshis(7));

        BasicRoute basicRoute1 = new BasicRoute(List.of(edge1), Coins.ofSatoshis(2));
        BasicRoute basicRoute2 = new BasicRoute(List.of(edge2), Coins.ofSatoshis(2));
        BasicRoute basicRoute3 = new BasicRoute(List.of(edge3), Coins.ofSatoshis(3));
        assertThat(fixedRoutes).containsExactlyInAnyOrder(
                new Route(basicRoute1),
                new Route(basicRoute2),
                new Route(basicRoute3)
        );
    }
}
