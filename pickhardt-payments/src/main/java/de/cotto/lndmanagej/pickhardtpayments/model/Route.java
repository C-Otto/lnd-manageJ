package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public record Route(
        List<Edge> edges,
        Coins amount,
        Coins fees,
        Map<Edge, EdgeWithLiquidityInformation> liquidityInformation
) {
    public Route(List<Edge> edges, Coins amount) {
        this(edges, amount, computeFees(edges, amount), Map.of());
    }

    public Route {
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, is " + amount);
        }
    }

    public double getProbability() {
        return edges.stream().map(this::getProbability).reduce(1.0, (a, b) -> a * b);
    }

    private Double getProbability(Edge edge) {
        EdgeWithLiquidityInformation edgeWithLiquidityInformation =
                liquidityInformation.getOrDefault(edge, getDefault(edge));
        boolean knownLiquidity = edgeWithLiquidityInformation.isKnownLiquidity();
        if (knownLiquidity) {
            if (amount.compareTo(edgeWithLiquidityInformation.availableLiquidityLowerBound()) <= 0) {
                return 1.0;
            }
            return 0.0;
        }
        long upperBoundSat = edgeWithLiquidityInformation.availableLiquidityUpperBound().milliSatoshis() / 1_000;
        long lowerBoundSat = edgeWithLiquidityInformation.availableLiquidityLowerBound().milliSatoshis() / 1_000;
        long amountSat = amount.milliSatoshis() / 1_000;
        return (1.0 * (upperBoundSat + 1 - amountSat)) / (upperBoundSat + 1 - lowerBoundSat);
    }

    public Route getForAmount(Coins newAmount) {
        return new Route(edges, newAmount).withLiquidityInformation(liquidityInformation.values());
    }

    public Route withLiquidityInformation(Collection<EdgeWithLiquidityInformation> liquidityInformation) {
        Map<Edge, EdgeWithLiquidityInformation> map =
                liquidityInformation.stream().collect(toMap(EdgeWithLiquidityInformation::edge, e -> e));
        return new Route(edges, amount, fees, map);
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

    private EdgeWithLiquidityInformation getDefault(Edge edge) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, edge.capacity());
    }
}
