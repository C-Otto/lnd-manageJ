package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcGraph;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.PubkeyAndFeeRate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GraphService {
    private static final Coins MIN_CAPACITY = Coins.ofSatoshis(10_000_000);
    private static final long MAX_FEE_RATE = 5_000;
    private static final int MIN_EDGES = 10;

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

    public List<PubkeyAndFeeRate> getNodesWithHighFeeRate() {
        Set<Edge> edges = grpcGraph.getChannelEdges().orElse(null);
        if (edges == null) {
            return List.of();
        }
        Map<Pubkey, Set<Edge>> candidates = getCandidateEdges(edges);
        return candidates.entrySet().parallelStream()
                .map(this::withAverageFeeRate)
                .sorted(Comparator.comparing(PubkeyAndFeeRate::feeRate).reversed())
                .toList();
    }

    private Map<Pubkey, Set<Edge>> getCandidateEdges(Set<Edge> edges) {
        Map<Pubkey, Set<Edge>> candidates = edges.parallelStream()
                .filter(e -> e.policy().enabled())
                .filter(e -> e.policy().feeRate() <= MAX_FEE_RATE)
                .filter(e -> e.policy().feeRate() > 0)
                .filter(e -> MIN_CAPACITY.compareTo(e.capacity()) <= 0)
                .collect(LinkedHashMap::new, this::add, this::combine);
        candidates.values().removeIf(s -> s.size() < MIN_EDGES);
        return candidates;
    }

    private void combine(
            Map<Pubkey, Set<Edge>> first,
            Map<Pubkey, Set<Edge>> second
    ) {
        for (Map.Entry<Pubkey, Set<Edge>> entry : second.entrySet()) {
            Pubkey pubkey = entry.getKey();
            first.computeIfAbsent(pubkey, k -> new LinkedHashSet<>()).addAll(entry.getValue());
        }
    }

    private void add(Map<Pubkey, Set<Edge>> map, Edge edge) {
        map.compute(edge.endNode(), (p, s) -> {
            if (s == null) {
                s = new LinkedHashSet<>();
            }
            s.add(edge);
            return s;
        });
    }

    private PubkeyAndFeeRate withAverageFeeRate(Map.Entry<Pubkey, Set<Edge>> entry) {
        Pubkey pubkey = entry.getKey();
        long feeRateSum = entry.getValue().stream().mapToLong(e -> e.policy().feeRate()).sum();
        int average = (int) (feeRateSum / entry.getValue().size());
        return new PubkeyAndFeeRate(pubkey, average);
    }

}
