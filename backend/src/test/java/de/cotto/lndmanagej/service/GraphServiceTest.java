package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.DirectedChannelEdgeFixtures.CHANNEL_EDGE_WITH_POLICY;
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
}
