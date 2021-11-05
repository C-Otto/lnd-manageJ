package de.cotto.lndmanagej.model;

import java.util.Objects;

public class ForwardFailure {
    private final HtlcDetails htlcDetails;
    private final ForwardAttempt forwardAttempt;

    public ForwardFailure(HtlcDetails htlcDetails, ForwardAttempt forwardAttempt) {
        this.htlcDetails = htlcDetails;
        this.forwardAttempt = forwardAttempt;
    }

    @Override
    public String toString() {
        return "ForwardFailure{" +
                "htlcDetails=" + htlcDetails +
                ", forwardAttempt=" + forwardAttempt +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ForwardFailure that = (ForwardFailure) other;
        return Objects.equals(htlcDetails, that.htlcDetails) && Objects.equals(forwardAttempt, that.forwardAttempt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(htlcDetails, forwardAttempt);
    }
}
