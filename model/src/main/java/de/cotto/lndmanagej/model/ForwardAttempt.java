package de.cotto.lndmanagej.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nullable;
import java.util.Objects;

public record ForwardAttempt(
        HtlcDetails htlcDetails,
        int incomingTimelock,
        int outgoingTimelock,
        Coins incomingAmount,
        Coins outgoingAmount
) {

    public static Builder builder() {
        return new Builder();
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

        @CanIgnoreReturnValue
        public Builder withIncomingTimelock(int incomingTimelock) {
            this.incomingTimelock = incomingTimelock;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withOutgoingTimelock(int outgoingTimelock) {
            this.outgoingTimelock = outgoingTimelock;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withIncomingAmount(long incomingAmtMsat) {
            this.incomingAmount = Coins.ofMilliSatoshis(incomingAmtMsat);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withOutgoingAmount(long outgoingAmtMsat) {
            this.outgoingAmount = Coins.ofMilliSatoshis(outgoingAmtMsat);
            return this;
        }

        @CanIgnoreReturnValue
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
