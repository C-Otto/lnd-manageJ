package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public record Route(
        List<Edge> edges,
        Coins amount,
        List<Coins> feesForHops,
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

    public Coins feesWithFirstHop() {
        if (edges.isEmpty()) {
            return Coins.NONE;
        }
        Coins forwardAmountForFirstHop = fees().add(amount);
        return fees().add(getFeesForEdgeAndAmount(forwardAmountForFirstHop, edges.get(0)));
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

    public Coins fees() {
        return feesForHops.stream().reduce(Coins.NONE, Coins::add);
    }

    public long getFeeRate() {
        return fees().milliSatoshis() * 1_000_000 / amount.milliSatoshis();
    }

    public long getFeeRateWithFirstHop() {
        return feesWithFirstHop().milliSatoshis() * 1_000_000 / amount.milliSatoshis();
    }

    public Route getForAmount(Coins newAmount) {
        return new Route(edges, newAmount).withLiquidityInformation(liquidityInformation.values());
    }

    public Route withLiquidityInformation(Collection<EdgeWithLiquidityInformation> liquidityInformation) {
        Map<Edge, EdgeWithLiquidityInformation> map =
                liquidityInformation.stream().collect(toMap(EdgeWithLiquidityInformation::edge, e -> e));
        return new Route(edges, amount, feesForHops, map);
    }

    public Coins feeForHop(int hopIndex) {
        return feesForHops.get(hopIndex);
    }

    public Coins forwardAmountForHop(int hopIndex) {
        Coins accumulatedFees = Coins.NONE;
        for (int i = hopIndex + 1; i < feesForHops.toArray().length; i++) {
            accumulatedFees = accumulatedFees.add(feeForHop(i));
        }
        return amount.add(accumulatedFees);
    }

    private static List<Coins> computeFees(List<Edge> edges, Coins amount) {
        Coins fees = Coins.NONE;
        Coins amountWithFees = amount;
        List<Coins> feesForHops = new ArrayList<>();
        feesForHops.add(Coins.NONE);
        for (int i = edges.size() - 1; i > 0; i--) {
            Coins feesForHop = getFeesForEdgeAndAmount(amountWithFees, edges.get(i));
            amountWithFees = amountWithFees.add(feesForHop);
            fees = fees.add(feesForHop);
            feesForHops.add(0, feesForHop);
        }
        return feesForHops;
    }

    private static Coins getFeesForEdgeAndAmount(Coins amountWithFees, Edge edge) {
        long feeRate = edge.policy().feeRate();
        Coins baseFeeForHop = edge.policy().baseFee();
        Coins relativeFees = Coins.ofMilliSatoshis(feeRate * amountWithFees.milliSatoshis() / 1_000_000);
        return baseFeeForHop.add(relativeFees);
    }

    private EdgeWithLiquidityInformation getDefault(Edge edge) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, edge.capacity());
    }
}
