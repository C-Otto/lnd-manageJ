package de.cotto.lndmanagej.pickhardtpayments.model;

import de.cotto.lndmanagej.model.Coins;

import java.util.Set;

public record MultiPathPayment(Coins amount, double probability, Set<Route> routes) {
    public static final MultiPathPayment FAILURE = new MultiPathPayment(Coins.NONE, 0, Set.of());
}
