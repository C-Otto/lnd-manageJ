package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Edge;

import java.util.ArrayList;
import java.util.List;

public record Route(
        List<Edge> edges,
        List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation,
        Coins amount,
        List<Coins> feesForHops
) {
    public Route(BasicRoute basicRoute) {
        this(basicRoute, basicRoute.edges().stream().map(Route::getDefaultLiquidityInformation).toList());
    }

    public Route(BasicRoute basicRoute, List<EdgeWithLiquidityInformation> edgesWithLiquidityInformation) {
        this(
                basicRoute.edges(),
                edgesWithLiquidityInformation,
                basicRoute.amount(),
                computeFees(basicRoute.edges(), basicRoute.amount())
        );
    }

    private static EdgeWithLiquidityInformation getDefaultLiquidityInformation(Edge edge) {
        return new EdgeWithLiquidityInformation(edge, Coins.NONE, edge.capacity());
    }

    public Route {
        if (edges.size() != edgesWithLiquidityInformation.size()) {
            throw new IllegalArgumentException("Edges must correspond to edges with liquidity information");
        }
        for (int i = 0; i < edges.size(); i++) {
            Edge edge = edges.get(i);
            EdgeWithLiquidityInformation edgeWithLiquidityInformation = edgesWithLiquidityInformation.get(i);
            if (!edge.equals(edgeWithLiquidityInformation.edge())) {
                throw new IllegalArgumentException("Edges must correspond to edges with liquidity information");
            }
        }
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
        return new Route(new BasicRoute(edges, newAmount), edgesWithLiquidityInformation);
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

    public int expiryForHop(int index, int blockHeight, int finalCltvDelta) {
        int timeLockDeltaNext = 0;
        int timeLockDelta = 0;
        for (int i = edges.size() - 1; i > index; i--) {
            timeLockDelta += timeLockDeltaNext;
            timeLockDeltaNext = edges.get(i).policy().timeLockDelta();
        }
        return blockHeight + finalCltvDelta + timeLockDelta;
    }

    public int totalTimeLock(int blockHeight, int finalCltvDelta) {
        return expiryForHop(-1, blockHeight, finalCltvDelta);
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
}
