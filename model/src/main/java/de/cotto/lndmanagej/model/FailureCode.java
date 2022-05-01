package de.cotto.lndmanagej.model;

import java.util.Arrays;

public enum FailureCode {
    INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS(1),
    CHANNEL_DISABLED(14),
    TEMPORARY_CHANNEL_FAILURE(15),
    UNKNOWN_NEXT_PEER(18),
    MPP_TIMEOUT(23),
    UNKNOWN_FAILURE(-1);

    private final int code;

    FailureCode(int code) {
        this.code = code;
    }

    public static FailureCode getFor(int code) {
        return Arrays.stream(values()).filter(value -> value.code == code).findFirst().orElse(UNKNOWN_FAILURE);
    }
}
