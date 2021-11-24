package de.cotto.lndmanagej.statistics.persistence;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public final class BalancesId implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    @Nullable
    private Long channelId;

    private long timestamp;

    public BalancesId() {
        // for JPA
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BalancesId that = (BalancesId) other;
        return timestamp == that.timestamp && Objects.equals(channelId, that.channelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelId, timestamp);
    }
}