package de.cotto.lndmanagej.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public enum FailureCode {
    INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS(1),
    FEE_INSUFFICIENT(12),
    CHANNEL_DISABLED(14),
    TEMPORARY_CHANNEL_FAILURE(15),
    UNKNOWN_NEXT_PEER(18),
    MPP_TIMEOUT(23),
    UNKNOWN_FAILURE(-1);

    private static final Logger LOGGER = LoggerFactory.getLogger(FailureCode.class);
    private final int code;

    FailureCode(int code) {
        this.code = code;
    }

    public static FailureCode getFor(int code) {
        return Arrays.stream(values()).filter(value -> value.code == code).findFirst().orElseGet(() -> {
            LOGGER.warn("Unknown failure code {}", code);
            return UNKNOWN_FAILURE;
        });
    }
}
