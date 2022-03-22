package de.cotto.lndmanagej.model;

public record PoliciesForLocalChannel(
        Policy local,
        Policy remote
) {
    public static final PoliciesForLocalChannel UNKNOWN = new PoliciesForLocalChannel(Policy.UNKNOWN, Policy.UNKNOWN);
}
