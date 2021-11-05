package de.cotto.lndmanagej.model;

public record ForwardFailure(
        HtlcDetails htlcDetails,
        ForwardAttempt forwardAttempt
) {
}
