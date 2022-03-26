package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICY_1;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowsFixtures.FLOWS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

class RoutesTest {
    private final Flows flows = new Flows();

    @Test
    void fromFlows_empty() {
        assertThat(Routes.fromFlows(PUBKEY, PUBKEY_2, flows)).isEmpty();
    }

    @Test
    void fromFlows_source_is_target() {
        assertThat(Routes.fromFlows(PUBKEY, PUBKEY, FLOWS)).isEmpty();
    }

    @Test
    void fromFlows_simple() {
        assertThat(Routes.fromFlows(PUBKEY, PUBKEY_2, new Flows(FLOW)))
                .containsExactly(new Route(List.of(EDGE), Coins.ofSatoshis(1)));
    }

    @Test
    void fromFlows_does_not_mutate_flows() {
        Coins expected = FLOW.amount();
        Routes.fromFlows(PUBKEY, PUBKEY_2, FLOWS);
        assertThat(FLOWS.getFlow(EDGE)).isEqualTo(expected);
    }

    @Test
    void fromFlows_impossible() {
        assertThat(Routes.fromFlows(PUBKEY_3, PUBKEY, FLOWS)).isEmpty();
    }

    @Test
    void fromFlows_two_parallel_channels() {
        Edge edge1 = createEdgeWithChannelId(CHANNEL_ID);
        Edge edge2 = createEdgeWithChannelId(CHANNEL_ID_2);
        flows.add(edge1, Coins.ofSatoshis(10));
        flows.add(edge2, Coins.ofSatoshis(20));
        assertThat(Routes.fromFlows(PUBKEY, PUBKEY_2, flows)).containsExactlyInAnyOrder(
                new Route(List.of(edge1), Coins.ofSatoshis(10)),
                new Route(List.of(edge2), Coins.ofSatoshis(20))
        );
    }

    @Test
    void fromFlows_two_channels_joining() {
        Edge edge1a = createEdgeWithChannelId(CHANNEL_ID);
        Edge edge1b = createEdgeWithChannelId(CHANNEL_ID_2);
        Edge edge2 = new Edge(CHANNEL_ID, PUBKEY_2, PUBKEY_3, CAPACITY, POLICY_1);
        flows.add(edge1a, Coins.ofSatoshis(10));
        flows.add(edge1b, Coins.ofSatoshis(10));
        flows.add(edge2, Coins.ofSatoshis(20));
        assertThat(Routes.fromFlows(PUBKEY, PUBKEY_3, flows)).containsExactlyInAnyOrder(
                new Route(List.of(edge1a, edge2), Coins.ofSatoshis(10)),
                new Route(List.of(edge1b, edge2), Coins.ofSatoshis(10))
        );
    }

    @Test
    void ensureTotalAmount_adds_to_only_route() {
        Set<Route> routes = Routes.fromFlows(PUBKEY, PUBKEY_2, FLOWS);
        assumeThat(routes).containsExactly(new Route(List.of(EDGE), Coins.ofSatoshis(1)));
        Routes.ensureTotalAmount(routes, Coins.ofSatoshis(2));
        assertThat(routes).containsExactly(new Route(List.of(EDGE), Coins.ofSatoshis(2)));
    }

    @Test
    void ensureTotalAmount_adds_to_route_with_highest_probability() {
        Flows flows = new Flows();
        flows.add(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1), Coins.ofSatoshis(2));
        flows.add(new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1), Coins.ofSatoshis(1));
        flows.add(new Edge(CHANNEL_ID_3, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1), Coins.ofSatoshis(3));
        Set<Route> routes = Routes.fromFlows(PUBKEY, PUBKEY_2, flows);
        Routes.ensureTotalAmount(routes, Coins.ofSatoshis(7));
        assertThat(routes).containsExactlyInAnyOrder(
                new Route(List.of(new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1)), Coins.ofSatoshis(2)),
                new Route(List.of(new Edge(CHANNEL_ID_2, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1)), Coins.ofSatoshis(2)),
                new Route(List.of(new Edge(CHANNEL_ID_3, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1)), Coins.ofSatoshis(3))
        );
    }

    private Edge createEdgeWithChannelId(ChannelId channelId) {
        return new Edge(channelId, PUBKEY, PUBKEY_2, CAPACITY, POLICY_1);
    }

}
