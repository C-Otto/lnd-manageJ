package de.cotto.lndmanagej.model;

public record FeeConfiguration(
        long outgoingFeeRate,
        Coins outgoingBaseFee,
        long incomingFeeRate,
        Coins incomingBaseFee,
        boolean enabledLocal,
        boolean enabledRemote
) {
}
