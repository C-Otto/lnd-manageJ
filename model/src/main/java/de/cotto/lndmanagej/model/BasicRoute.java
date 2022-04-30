package de.cotto.lndmanagej.model;

import java.util.List;

public record BasicRoute(List<Edge> edges, Coins amount) {
    public BasicRoute {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, is " + amount);
        }
    }
}
