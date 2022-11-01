package de.cotto.lndmanagej.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;

public record HtlcDetails(
        ChannelId incomingChannelId,
        ChannelId outgoingChannelId,
        long incomingHtlcId,
        long outgoingHtlcId,
        Instant timestamp
) {

    public static Builder builder() {
        return new Builder();
    }

    public HtlcDetails withoutTimestamp() {
        return new HtlcDetails(
                incomingChannelId, outgoingChannelId, incomingHtlcId, outgoingHtlcId, Instant.ofEpochMilli(0)
        );
    }

    public static class Builder {
        @Nullable
        private ChannelId incomingChannelId;

        @Nullable
        private ChannelId outgoingChannelId;

        private long incomingHtlcId;

        private long outgoingHtlcId;

        @Nullable
        private Instant timestamp;

        @CanIgnoreReturnValue
        public Builder withIncomingChannelId(long incomingChannelId) {
            this.incomingChannelId = ChannelId.fromShortChannelId(incomingChannelId);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withOutgoingChannelId(long outgoingChannelId) {
            this.outgoingChannelId = ChannelId.fromShortChannelId(outgoingChannelId);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withIncomingHtlcId(long incomingHtlcId) {
            this.incomingHtlcId = incomingHtlcId;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withOutgoingHtlcId(long outgoingHtlcId) {
            this.outgoingHtlcId = outgoingHtlcId;
            return this;
        }

        @CanIgnoreReturnValue
        public Builder withTimestamp(long timestampNs) {
            this.timestamp = Instant.ofEpochSecond(0, timestampNs);
            return this;
        }

        public HtlcDetails build() {
            return new HtlcDetails(
                    Objects.requireNonNull(incomingChannelId),
                    Objects.requireNonNull(outgoingChannelId),
                    incomingHtlcId,
                    outgoingHtlcId,
                    Objects.requireNonNull(timestamp)
            );
        }
    }
}
