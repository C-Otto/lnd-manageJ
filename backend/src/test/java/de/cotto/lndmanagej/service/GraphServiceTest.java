package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.DirectedChannelEdge;
import de.cotto.lndmanagej.model.Policy;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.PubkeyAndFeeRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.DirectedChannelEdgeFixtures.CHANNEL_EDGE_WITH_POLICY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_4;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {
    @InjectMocks
    private GraphService graphService;

    @Mock
    private GrpcGraph grpcGraph;

    @Test
    void getNumberOfEdges_default() {
        assertThat(graphService.getNumberOfChannels()).isEqualTo(0);
    }

    @Test
    void getNumberOfEdges() {
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(Set.of(CHANNEL_EDGE_WITH_POLICY)));
        assertThat(graphService.getNumberOfChannels()).isEqualTo(1);
    }

    @Test
    void resetCache() {
        graphService.resetCache();
        verify(grpcGraph).resetCache();
    }

    @Test
    void getNodesWithHighFeeRate_empty() {
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_empty_optional() {
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_no_edge() {
        Set<DirectedChannelEdge> edges = Set.of();
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(edges));
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_not_enough_edges() {
        edges(9, 200, PUBKEY_2);
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_ignores_disabled_channels() {
        Set<DirectedChannelEdge> edges = edges(9, 200, PUBKEY_2);
        Policy policy = new Policy(2_000, Coins.NONE, false, 40, Coins.ofMilliSatoshis(1), Coins.ofSatoshis(10_000));
        edges.add(edge(policy, Coins.ofSatoshis(10_000_000), PUBKEY_2));
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_ignores_absurd_fee_rates() {
        Set<DirectedChannelEdge> edges = edges(9, 200, PUBKEY_2);
        edges.add(edge(20_000, 20, PUBKEY_2));
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_ignores_zero_fee_rate() {
        Set<DirectedChannelEdge> edges = edges(9, 200, PUBKEY_2);
        edges.add(edge(0, 20, PUBKEY_2));
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate_ignores_low_capacity_channels() {
        Set<DirectedChannelEdge> edges = edges(9, 200, PUBKEY_2);
        edges.add(edge(200, 9, PUBKEY_2));
        assertThat(graphService.getNodesWithHighFeeRate()).isEmpty();
    }

    @Test
    void getNodesWithHighFeeRate() {
        edges(10, 300, PUBKEY_3);
        assertThat(graphService.getNodesWithHighFeeRate()).containsExactly(
                new PubkeyAndFeeRate(PUBKEY_3, 300)
        );
    }

    @Test
    void getNodesWithHighFeeRate_average() {
        Set<DirectedChannelEdge> edges = new LinkedHashSet<>();
        for (int i = 0; i < 10; i++) {
            edges.add(edge(300 + i * 10, 20, PUBKEY_4));
            edges.add(edge(200, 20 + i, PUBKEY_3));
        }
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(edges));
        assertThat(graphService.getNodesWithHighFeeRate()).containsExactly(
                new PubkeyAndFeeRate(PUBKEY_4, 345),
                new PubkeyAndFeeRate(PUBKEY_3, 200)
        );
    }

    private Set<DirectedChannelEdge> edges(int count, int feeRate, Pubkey target) {
        Set<DirectedChannelEdge> edges = new LinkedHashSet<>();
        for (int i = 0; i < count; i++) {
            edges.add(edge(feeRate, 20 + i, target));
        }
        when(grpcGraph.getChannelEdges()).thenReturn(Optional.of(edges));
        return edges;
    }

    private DirectedChannelEdge edge(long feeRate, long capacityMillionSat, Pubkey target) {
        Policy policy = new Policy(feeRate, Coins.NONE, true, 40, Coins.ofMilliSatoshis(1), Coins.ofSatoshis(10_000));
        Coins capacity = Coins.ofSatoshis(capacityMillionSat * 1_000_000);
        return edge(policy, capacity, target);
    }

    private DirectedChannelEdge edge(Policy policy, Coins capacity, Pubkey target) {
        return new DirectedChannelEdge(
                CHANNEL_ID,
                capacity,
                PUBKEY,
                target,
                policy,
                Policy.UNKNOWN
        );
    }
}
