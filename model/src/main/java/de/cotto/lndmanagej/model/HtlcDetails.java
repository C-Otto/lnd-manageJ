package de.cotto.lndmanagej.model;

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

        public Builder withIncomingChannelId(long incomingChannelId) {
            this.incomingChannelId = ChannelId.fromShortChannelId(incomingChannelId);
            return this;
        }

        public Builder withOutgoingChannelId(long outgoingChannelId) {
            this.outgoingChannelId = ChannelId.fromShortChannelId(outgoingChannelId);
            return this;
        }

        public Builder withIncomingHtlcId(long incomingHtlcId) {
            this.incomingHtlcId = incomingHtlcId;
            return this;
        }

        public Builder withOutgoingHtlcId(long outgoingHtlcId) {
            this.outgoingHtlcId = outgoingHtlcId;
            return this;
        }

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
