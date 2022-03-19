package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.List;

public record Route(List<Edge> edges, Coins amount) {
    public Route {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, is " + amount);
        }
    }

    public double getProbability() {
        return edges.stream().map(edge -> {
            long capacitySat = edge.capacity().satoshis();
            return (1.0 * (capacitySat + 1 - amount.satoshis())) / (capacitySat + 1);
        }).reduce(1.0, (a, b) -> a * b);
    }

    public Route getForAmount(Coins newAmount) {
        return new Route(edges, newAmount);
    }
}
