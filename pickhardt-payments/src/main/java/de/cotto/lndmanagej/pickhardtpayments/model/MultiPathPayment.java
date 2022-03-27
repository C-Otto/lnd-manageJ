package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.Set;

public record MultiPathPayment(Coins amount, Coins fees, double probability, Set<Route> routes) {
    public static final MultiPathPayment FAILURE = new MultiPathPayment(Coins.NONE, Coins.NONE, 0.0, Set.of());

    public MultiPathPayment(Set<Route> routes) {
        this(
                routes.stream().map(Route::amount).reduce(Coins.NONE, Coins::add),
                routes.stream().map(Route::fees).reduce(Coins.NONE, Coins::add),
                routes.stream().mapToDouble(Route::getProbability).reduce(1.0, (a, b) -> a * b),
                routes
        );
    }

    public long getFeeRate() {
        return fees.milliSatoshis() * 1_000_000 / amount.milliSatoshis();
    }
}
