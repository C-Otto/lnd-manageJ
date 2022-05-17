package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGraph;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class GraphService {
    private final GrpcGraph grpcGraph;

    public GraphService(GrpcGraph grpcGraph) {
        this.grpcGraph = grpcGraph;
    }

    public int getNumberOfChannels() {
        return grpcGraph.getChannelEdges().map(Set::size).orElse(0);
    }

    public void resetCache() {
        grpcGraph.resetCache();
    }
}
