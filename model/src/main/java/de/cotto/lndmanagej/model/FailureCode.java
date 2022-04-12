package de.cotto.lndmanagej.model;

public record FailureCode(int code) {
    public static final FailureCode TEMPORARY_CHANNEL_FAILURE = new FailureCode(15);
}
