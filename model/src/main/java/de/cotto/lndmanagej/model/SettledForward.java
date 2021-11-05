package de.cotto.lndmanagej.model;

import java.util.Objects;

public class SettledForward {
    private final HtlcDetails htlcDetails;
    private final ForwardAttempt attempt;

    public SettledForward(HtlcDetails htlcDetails, ForwardAttempt attempt) {
        this.htlcDetails = htlcDetails;
        this.attempt = attempt;
    }

    @Override
    public String toString() {
        return "SettledForward{" +
                "htlcDetails=" + htlcDetails +
                ", attempt=" + attempt +
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
        SettledForward that = (SettledForward) other;
        return Objects.equals(htlcDetails, that.htlcDetails) && Objects.equals(attempt, that.attempt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(htlcDetails, attempt);
    }
}
