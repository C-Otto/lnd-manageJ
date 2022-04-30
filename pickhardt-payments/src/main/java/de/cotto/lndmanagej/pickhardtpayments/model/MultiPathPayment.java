package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.List;

public record MultiPathPayment(
        Coins amount,
        Coins fees,
        Coins feesWithFirstHop,
        double probability,
        List<Route> routes
) {
    public static final MultiPathPayment FAILURE =
            new MultiPathPayment(Coins.NONE, Coins.NONE, Coins.NONE, 0.0, List.of());

    public MultiPathPayment(List<Route> routes) {
        this(
                routes.stream().map(Route::getAmount).reduce(Coins.NONE, Coins::add),
                routes.stream().map(Route::getFees).reduce(Coins.NONE, Coins::add),
                routes.stream().map(Route::getFeesWithFirstHop).reduce(Coins.NONE, Coins::add),
                routes.stream().mapToDouble(Route::getProbability).reduce(1.0, (a, b) -> a * b),
                routes
        );
    }

    public boolean isFailure() {
        return equals(FAILURE);
    }

    public long getFeeRate() {
        return getFeeRateForFees(fees);
    }

    public long getFeeRateWithFirstHop() {
        return getFeeRateForFees(feesWithFirstHop);
    }

    private long getFeeRateForFees(Coins feesToConsider) {
        if (amount.isPositive()) {
            return feesToConsider.milliSatoshis() * 1_000_000 / amount.milliSatoshis();
        }
        return 0;
    }
}
