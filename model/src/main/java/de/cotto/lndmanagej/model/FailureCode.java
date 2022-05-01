package de.cotto.lndmanagej.model;

public record FailureCode(int code) {
    public static final FailureCode CHANNEL_DISABLED = new FailureCode(14);
    public static final FailureCode TEMPORARY_CHANNEL_FAILURE = new FailureCode(15);
    public static final FailureCode UNKNOWN_NEXT_PEER = new FailureCode(18);
}
