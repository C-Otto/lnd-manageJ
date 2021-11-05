package de.cotto.lndmanagej.model;

import javax.annotation.Nullable;
import java.util.Objects;

public class ForwardAttempt {
    private final HtlcDetails htlcDetails;
    private final int incomingTimelock;
    private final int outgoingTimelock;
    private final Coins incomingAmount;
    private final Coins outgoingAmount;

    private ForwardAttempt(
            HtlcDetails htlcDetails,
            int incomingTimelock,
            int outgoingTimelock,
            Coins incomingAmount,
            Coins outgoingAmount
    ) {
        this.htlcDetails = htlcDetails;
        this.incomingTimelock = incomingTimelock;
        this.outgoingTimelock = outgoingTimelock;
        this.incomingAmount = incomingAmount;
        this.outgoingAmount = outgoingAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "ForwardAttempt{" +
                "htlcDetails=" + htlcDetails +
                ", incomingTimelock=" + incomingTimelock +
                ", outgoingTimelock=" + outgoingTimelock +
                ", incomingAmount=" + incomingAmount +
                ", outgoingAmount=" + outgoingAmount +
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
        ForwardAttempt that = (ForwardAttempt) other;
        return incomingTimelock == that.incomingTimelock
                && outgoingTimelock == that.outgoingTimelock
                && Objects.equals(htlcDetails, that.htlcDetails)
                && Objects.equals(incomingAmount, that.incomingAmount)
                && Objects.equals(outgoingAmount, that.outgoingAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(htlcDetails, incomingTimelock, outgoingTimelock, incomingAmount, outgoingAmount);
    }

    public static class Builder {
        private int incomingTimelock;

        private int outgoingTimelock;

        @Nullable
        private Coins incomingAmount;

        @Nullable
        private Coins outgoingAmount;

        @Nullable
        private HtlcDetails htlcDetails;

        public Builder withIncomingTimelock(int incomingTimelock) {
            this.incomingTimelock = incomingTimelock;
            return this;
        }

        public Builder withOutgoingTimelock(int outgoingTimelock) {
            this.outgoingTimelock = outgoingTimelock;
            return this;
        }

        public Builder withIncomingAmount(long incomingAmtMsat) {
            this.incomingAmount = Coins.ofMilliSatoshis(incomingAmtMsat);
            return this;
        }

        public Builder withOutgoingAmount(long outgoingAmtMsat) {
            this.outgoingAmount = Coins.ofMilliSatoshis(outgoingAmtMsat);
            return this;
        }

        public Builder withHtlcDetails(HtlcDetails htlcDetails) {
            this.htlcDetails = htlcDetails;
            return this;
        }

        public ForwardAttempt build() {
            return new ForwardAttempt(
                    Objects.requireNonNull(htlcDetails),
                    incomingTimelock,
                    outgoingTimelock,
                    Objects.requireNonNull(incomingAmount),
                    Objects.requireNonNull(outgoingAmount)
            );
        }
    }
}
