package de.cotto.lndmanagej.model;

public record SettledForward(
        HtlcDetails htlcDetails,
        ForwardAttempt forwardAttempt
) {
}
