package de.cotto.lndmanagej.model;

public record Policies(
        Policy local,
        Policy remote
) {
    public static final Policies UNKNOWN = new Policies(Policy.UNKNOWN, Policy.UNKNOWN);
}
