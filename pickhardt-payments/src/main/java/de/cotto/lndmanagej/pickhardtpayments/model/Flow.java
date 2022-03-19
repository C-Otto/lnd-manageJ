package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

public record Flow(Edge edge, Coins amount) {
    public Flow {
        if (amount.isNonPositive()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (edge.startNode().equals(edge.endNode())) {
            throw new IllegalArgumentException("Source and target must be different");
        }
    }

    public double getProbability() {
        long capacitySat = edge.capacity().satoshis();
        return 1.0 * (capacitySat + 1 - amount.satoshis()) / (capacitySat + 1);
    }
}
