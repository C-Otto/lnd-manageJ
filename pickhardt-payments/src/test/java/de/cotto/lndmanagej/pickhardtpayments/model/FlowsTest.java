package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_5;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_1_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_2_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.EdgeFixtures.EDGE_3_4;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW_2;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowFixtures.FLOW_3;
import static de.cotto.lndmanagej.pickhardtpayments.model.FlowsFixtures.FLOWS;
import static org.assertj.core.api.Assertions.assertThat;

class FlowsTest {
    @Test
    void isEmpty() {
        assertThat(new Flows().isEmpty()).isTrue();
    }

    @Test
    void isEmpty_false() {
        assertThat(new Flows(FLOW).isEmpty()).isFalse();
    }

    @Test
    void add_to_empty() {
        Flows flows = new Flows();
        flows.add(FLOW);
        assertThat(flows.isEmpty()).isFalse();
    }

    @Test
    void add_amounts_cancel_each_other() {
        Flows flows = new Flows();
        flows.add(EDGE, Coins.ofSatoshis(1));
        flows.add(EDGE, Coins.ofSatoshis(-1));
        assertThat(flows.isEmpty()).isTrue();
    }

    @Test
    void add_different_channel() {
        Flows flows = new Flows();
        flows.add(new Flow(EDGE, Coins.ofSatoshis(2)));
        flows.add(new Flow(EDGE_1_3, Coins.ofSatoshis(1)));
        assertThat(flows.getFlowsFrom(EDGE.startNode())).hasSize(2);
    }

    @Test
    void getFlowsFrom() {
        Flows flows = new Flows(FLOW);
        assertThat(flows.getFlowsFrom(FLOW.edge().startNode())).containsExactly(FLOW);
    }

    @Test
    void constructor_combines_flows_along_same_channel() {
        Flows flows = new Flows(FLOW, FLOW);
        assertThat(flows.getFlowsFrom(FLOW.edge().startNode())).containsExactly(new Flow(EDGE, Coins.ofSatoshis(2)));
    }

    @Test
    void add_combines_flows_along_same_channel() {
        Flows flows = new Flows(FLOW);
        flows.add(FLOW);
        assertThat(flows.getFlowsFrom(FLOW.edge().startNode())).containsExactly(new Flow(EDGE, Coins.ofSatoshis(2)));
    }

    @Test
    void getFlow_no_entry_for_pubkey() {
        assertThat(new Flows().getFlow(EDGE)).isEqualTo(Coins.NONE);
    }

    @Test
    void getFlow_no_entry_for_edge() {
        Flows flows = new Flows(FLOW);
        assertThat(flows.getFlow(EDGE_1_3)).isEqualTo(Coins.NONE);
    }

    @Test
    void getFlow() {
        Flows flows = new Flows(FLOW);
        assertThat(flows.getFlow(EDGE)).isEqualTo(FLOW.amount());
    }

    @Test
    void getProbability() {
        Flows flows = new Flows(FLOW);
        assertThat(flows.getProbability()).isEqualTo(FLOW.getProbability());
    }

    @Test
    void getProbability_two_separate_flows() {
        Flows flows = new Flows(FLOW, FLOW_2);
        assertThat(flows.getProbability()).isEqualTo(FLOW.getProbability() * FLOW_2.getProbability());
    }

    @Test
    void getProbability_added_amount() {
        Flows flows = new Flows(FLOW, FLOW);
        long flowAmount = 2 * FLOW.amount().satoshis();
        long capacitySat = FLOW.edge().capacity().satoshis();
        double expected = 1.0 * (capacitySat + 1 - flowAmount) / (capacitySat + 1);
        assertThat(flows.getProbability()).isEqualTo(expected);
    }

    @Test
    void getCopy() {
        Flows original = new Flows(FLOW);
        Flows copy = original.getCopy();
        assertThat(copy.getFlow(FLOW.edge())).isEqualTo(FLOW.amount());
    }

    @Test
    void getCopy_changing_copy_does_not_change_original() {
        Flows original = new Flows(FLOW);
        Flows copy = original.getCopy();
        copy.add(FLOW_3);
        assertThat(copy.getFlow(FLOW_3.edge())).isEqualTo(FLOW_3.amount());
        assertThat(original.getFlow(FLOW_3.edge())).isEqualTo(Coins.NONE);
    }

    @Test
    void getCopy_changing_original_does_not_change_copy() {
        Flows original = new Flows(FLOW);
        Flows copy = original.getCopy();
        original.add(FLOW_2);
        assertThat(original.getFlow(FLOW_2.edge())).isEqualTo(FLOW_2.amount());
        assertThat(copy.getFlow(FLOW_2.edge())).isEqualTo(Coins.NONE);
    }

    @Test
    void getShortestPath_already_there() {
        assertThat(FLOWS.getShortestPath(PUBKEY, PUBKEY)).isEmpty();
    }

    @Test
    void getShortestPath_unreachable() {
        assertThat(FLOWS.getShortestPath(PUBKEY_4, PUBKEY)).isEmpty();
    }

    @Test
    void getShortestPath_simple() {
        assertThat(FLOWS.getShortestPath(PUBKEY, PUBKEY_4)).containsExactly(EDGE, EDGE_2_3, EDGE_3_4);
    }

    @Test
    void getShortestPath_complex() {
        Coins coins = Coins.ofSatoshis(1);
        Edge edge1to2 = new Edge(CHANNEL_ID, PUBKEY, PUBKEY_2, CAPACITY);
        Edge edge2to3 = new Edge(CHANNEL_ID_2, PUBKEY_2, PUBKEY_3, CAPACITY);
        Edge edge2to1 = new Edge(CHANNEL_ID_3, PUBKEY_2, PUBKEY, CAPACITY);
        Edge edge1to3 = new Edge(CHANNEL_ID_4, PUBKEY, PUBKEY_3, CAPACITY);
        Edge edge3to4 = new Edge(CHANNEL_ID_5, PUBKEY_3, PUBKEY_4, CAPACITY);
        Flows flows = new Flows(
                new Flow(edge1to2, coins),
                new Flow(edge2to3, coins),
                new Flow(edge2to1, coins),
                new Flow(edge1to3, coins),
                new Flow(edge3to4, coins)
        );
        assertThat(flows.getShortestPath(PUBKEY, PUBKEY_4)).containsExactly(edge1to3, edge3to4);
    }

    @Test
    void testEquals() {
        // https://github.com/jqno/equalsverifier/issues/613
        EqualsVerifier.forClass(Flows.class).withPrefabValues(Flow.class, FLOW, FLOW_2).usingGetClass().verify();
    }

    @Test
    void testToString() {
        assertThat(FLOWS).hasToString(
                "Flows{map={" +
                        PUBKEY + "={" + EDGE + "=Flow[edge=" + EDGE + ", amount=1.000]}, " +
                        PUBKEY_2 + "={" + EDGE_2_3 + "=Flow[edge=" + EDGE_2_3 + ", amount=2.000]}, " +
                        PUBKEY_3 + "={" + EDGE_3_4 + "=Flow[edge=" + EDGE_3_4 + ", amount=3.000]}" +
                        "}}"
        );
    }
}
