package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.List;

public record Route(List<Edge> edges, Coins amount, Coins fees) {
    public Route(List<Edge> edges, Coins amount) {
        this(edges, amount, computeFees(edges, amount));
    }

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

    private static Coins computeFees(List<Edge> edges, Coins amount) {
        Coins fees = Coins.NONE;
        Coins amountWithFees = amount;
        for (int i = edges.size() - 1; i >= 0; i--) {
            Edge edge = edges.get(i);
            long feeRate = edge.policy().feeRate();
            Coins baseFeeForHop = edge.policy().baseFee();
            Coins relativeFees = Coins.ofMilliSatoshis(feeRate * amountWithFees.milliSatoshis() / 1_000_000);
            Coins feesForHop = baseFeeForHop.add(relativeFees);
            amountWithFees = amountWithFees.add(feesForHop);
            fees = fees.add(feesForHop);
        }
        return fees;
    }
}
