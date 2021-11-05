package de.cotto.lndmanagej.model;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Objects;

public class HtlcDetails {
    private final ChannelId incomingChannelId;
    private final ChannelId outgoingChannelId;
    private final long incomingHtlcId;
    private final long outgoingHtlcId;
    private final Instant timestamp;

    private HtlcDetails(
            ChannelId incomingChannelId,
            ChannelId outgoingChannelId,
            long incomingHtlcId,
            long outgoingHtlcId,
            Instant timestamp
    ) {
        this.incomingChannelId = incomingChannelId;
        this.outgoingChannelId = outgoingChannelId;
        this.incomingHtlcId = incomingHtlcId;
        this.outgoingHtlcId = outgoingHtlcId;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "HtlcDetails{" +
                "incomingChannelId=" + incomingChannelId +
                ", outgoingChannelId=" + outgoingChannelId +
                ", incomingHtlcId=" + incomingHtlcId +
                ", outgoingHtlcId=" + outgoingHtlcId +
                ", timestamp=" + timestamp +
                '}';
    }

    public HtlcDetails withoutTimestamp() {
        return new HtlcDetails(
                incomingChannelId, outgoingChannelId, incomingHtlcId, outgoingHtlcId, Instant.ofEpochMilli(0)
        );
    }

    public Instant getTimestamp() {
        return timestamp;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        HtlcDetails that = (HtlcDetails) other;
        return incomingHtlcId == that.incomingHtlcId
                && outgoingHtlcId == that.outgoingHtlcId
                && Objects.equals(incomingChannelId, that.incomingChannelId)
                && Objects.equals(outgoingChannelId, that.outgoingChannelId)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(incomingChannelId, outgoingChannelId, incomingHtlcId, outgoingHtlcId, timestamp);
    }
}
