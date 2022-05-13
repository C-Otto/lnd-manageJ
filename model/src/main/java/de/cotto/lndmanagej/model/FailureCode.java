package de.cotto.lndmanagej.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public enum FailureCode {
    INCORRECT_OR_UNKNOWN_PAYMENT_DETAILS(1),
    INCORRECT_PAYMENT_AMOUNT(2),
    FINAL_INCORRECT_CLTV_EXPIRY(3),
    FINAL_INCORRECT_HTLC_AMOUNT(4),
    FINAL_EXPIRY_TOO_SOON(5),
    INVALID_REALM(6),
    EXPIRY_TOO_SOON(7),
    INVALID_ONION_VERSION(8),
    INVALID_ONION_HMAC(9),
    INVALID_ONION_KEY(10),
    AMOUNT_BELOW_MINIMUM(11),
    FEE_INSUFFICIENT(12),
    INCORRECT_CLTV_EXPIRY(13),
    CHANNEL_DISABLED(14),
    TEMPORARY_CHANNEL_FAILURE(15),
    REQUIRED_NODE_FEATURE_MISSING(16),
    REQUIRED_CHANNEL_FEATURE_MISSING(17),
    UNKNOWN_NEXT_PEER(18),
    TEMPORARY_NODE_FAILURE(19),
    PERMANENT_NODE_FAILURE(20),
    PERMANENT_CHANNEL_FAILURE(21),
    EXPIRY_TOO_FAR(22),
    MPP_TIMEOUT(23),
    INVALID_ONION_PAYLOAD(24),
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
