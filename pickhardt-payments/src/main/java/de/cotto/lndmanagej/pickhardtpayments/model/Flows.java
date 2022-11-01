package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Pubkey;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Flows {
    private final Map<Pubkey, Map<Edge, Flow>> map;

    public Flows() {
        map = new LinkedHashMap<>();
    }

    public Flows(Flow... flows) {
        map = new LinkedHashMap<>();
        Arrays.stream(flows).forEach(this::add);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Coins getFlow(Edge edge) {
        Flow flow = map.getOrDefault(edge.startNode(), Map.of()).get(edge);
        if (flow == null) {
            return Coins.NONE;
        }
        return flow.amount();
    }

    public void add(Flow flow) {
        add(flow.edge(), flow.amount());
    }

    public void add(Edge edge, Coins amount) {
        map.compute(edge.startNode(), (p, innerMap) -> {
            Map<Edge, Flow> newValue = new LinkedHashMap<>();
            if (innerMap == null) {
                newValue.put(edge, new Flow(edge, amount));
                return newValue;
            }
            innerMap.compute(edge, (e, f) -> {
                if (f == null) {
                    return new Flow(edge, amount);
                }
                Coins combinedAmount = amount.add(f.amount());
                if (combinedAmount.equals(Coins.NONE)) {
                    return null;
                }
                return new Flow(edge, combinedAmount);
            });
            if (innerMap.isEmpty()) {
                return null;
            }
            return innerMap;
        });
    }

    public Set<Flow> getFlowsFrom(Pubkey pubkey) {
        return new LinkedHashSet<>(map.getOrDefault(pubkey, Map.of()).values());
    }

    public List<Edge> getShortestPath(Pubkey source, Pubkey target) {
        Set<Pubkey> seen = new LinkedHashSet<>();
        seen.add(source);

        Map<Pubkey, List<Edge>> routes = new LinkedHashMap<>();
        routes.put(source, List.of());

        Queue<Pubkey> todo = new ArrayDeque<>();
        todo.add(source);

        while (!todo.isEmpty()) {
            Pubkey pubkey = todo.remove();
            if (pubkey.equals(target)) {
                return requireNonNull(routes.get(pubkey));
            }
            List<Edge> route = routes.get(pubkey);
            for (Flow flow : getFlowsFrom(pubkey)) {
                Edge edge = flow.edge();
                Pubkey successor = edge.endNode();
                if (seen.add(successor)) {
                    List<Edge> newRoute = new ArrayList<>(route);
                    newRoute.add(edge);
                    routes.put(successor, newRoute);
                    todo.add(successor);
                }
            }
        }
        return List.of();
    }

    public Flows getCopy() {
        Flows copy = new Flows();
        map.values().stream().flatMap(m -> m.values().stream()).forEach(copy::add);
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Flows otherFlow = (Flows) other;
        return Objects.equals(map, otherFlow.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(map);
    }

    @Override
    public String toString() {
        return "Flows{" +
                "map=" + map +
                '}';
    }
}
