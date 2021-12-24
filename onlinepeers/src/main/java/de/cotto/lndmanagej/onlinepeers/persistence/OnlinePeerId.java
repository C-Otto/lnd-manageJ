package de.cotto.lndmanagej.onlinepeers.persistence;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings("unused")
public final class OnlinePeerId implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    @Nullable
    private String pubkey;

    private long timestamp;

    public OnlinePeerId() {
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
        OnlinePeerId that = (OnlinePeerId) other;
        return timestamp == that.timestamp && Objects.equals(pubkey, that.pubkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pubkey, timestamp);
    }
}