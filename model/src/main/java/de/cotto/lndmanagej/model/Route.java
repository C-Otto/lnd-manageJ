package de.cotto.lndmanagej.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Route {
    private final List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation;
    private final Coins amount;
    private final List<Coins> feesForHops;

    public Route(BasicRoute basicRoute) {
        this(basicRoute.edges().stream().map(Route::getDefaultLiquidityInformation).toList(), basicRoute.amount());
    }

    public Route(List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation, Coins amount) {
        this.edgesWithLiquidityInformation = edgesWithLiquidityInformation;
        this.amount = amount;
        feesForHops = computeFees(edgesWithLiquidityInformation, amount);
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("Amount must be positive, is " + amount);
        }
    }

    private static EdgeWithLiquidityInformation getDefaultLiquidityInformation(Edge edge) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, edge.capacity());
    }

    public List<Edge> getEdges() {
        return edgesWithLiquidityInformation.stream().map(EdgeWithLiquidityInformation::edge).toList();
    }

    public List<EdgeWithLiquidityInformation> getEdgesWithLiquidityInformation() {
        return edgesWithLiquidityInformation.stream().toList();
    }

    public Coins getAmount() {
        return amount;
    }

    public Coins getFeesWithFirstHop() {
        if (edgesWithLiquidityInformation.isEmpty()) {
            return Coins.NONE;
        }
        Coins forwardAmountForFirstHop = getFees().add(amount);
        return getFees().add(getFeesForEdgeAndAmount(forwardAmountForFirstHop, edgesWithLiquidityInformation.get(0)));
    }

    public double getProbability() {
        return edgesWithLiquidityInformation.stream().map(this::getProbability).reduce(1.0, (a, b) -> a * b);
    }

    private Double getProbability(EdgeWithLiquidityInformation edge) {
        boolean knownLiquidity = edge.isKnownLiquidity();
        if (knownLiquidity) {
            if (amount.compareTo(edge.availableLiquidityLowerBound()) <= 0) {
                return 1.0;
            }
            return 0.0;
        }
        long upperBoundSat = edge.availableLiquidityUpperBound().milliSatoshis() / 1_000;
        long lowerBoundSat = edge.availableLiquidityLowerBound().milliSatoshis() / 1_000;
        long amountSat = amount.milliSatoshis() / 1_000;
        return (1.0 * (upperBoundSat + 1 - amountSat)) / (upperBoundSat + 1 - lowerBoundSat);
    }

    public Coins getFees() {
        return feesForHops.stream().reduce(Coins.NONE, Coins::add);
    }

    public long getFeeRate() {
        return getFees().milliSatoshis() * 1_000_000 / amount.milliSatoshis();
    }

    public long getFeeRateWithFirstHop() {
        return getFeesWithFirstHop().milliSatoshis() * 1_000_000 / amount.milliSatoshis();
    }

    public Route getForAmount(Coins newAmount) {
        return new Route(edgesWithLiquidityInformation, newAmount);
    }

    public Coins getFeeForHop(int hopIndex) {
        return feesForHops.get(hopIndex);
    }

    public Coins getForwardAmountForHop(int hopIndex) {
        Coins accumulatedFees = Coins.NONE;
        for (int i = hopIndex + 1; i < feesForHops.toArray().length; i++) {
            accumulatedFees = accumulatedFees.add(getFeeForHop(i));
        }
        return amount.add(accumulatedFees);
    }

    public int getExpiryForHop(int index, int blockHeight, int finalCltvDelta) {
        int timeLockDeltaNext = 0;
        int timeLockDelta = 0;
        for (int i = edgesWithLiquidityInformation.size() - 1; i > index; i--) {
            timeLockDelta += timeLockDeltaNext;
            timeLockDeltaNext = edgesWithLiquidityInformation.get(i).policy().timeLockDelta();
        }
        return blockHeight + finalCltvDelta + timeLockDelta;
    }

    public int getTotalTimeLock(int blockHeight, int finalCltvDelta) {
        return getExpiryForHop(-1, blockHeight, finalCltvDelta);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Route route = (Route) other;
        return Objects.equals(edgesWithLiquidityInformation, route.edgesWithLiquidityInformation)
                && Objects.equals(amount, route.amount)
                && Objects.equals(feesForHops, route.feesForHops);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgesWithLiquidityInformation, amount, feesForHops);
    }

    private static List<Coins> computeFees(List<EdgeWithLiquidityInformation> edges, Coins amount) {
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

    private static Coins getFeesForEdgeAndAmount(Coins amountWithFees, EdgeWithLiquidityInformation edge) {
        long feeRate = edge.policy().feeRate();
        Coins baseFeeForHop = edge.policy().baseFee();
        Coins relativeFees = Coins.ofMilliSatoshis(feeRate * amountWithFees.milliSatoshis() / 1_000_000);
        return baseFeeForHop.add(relativeFees);
    }
}
