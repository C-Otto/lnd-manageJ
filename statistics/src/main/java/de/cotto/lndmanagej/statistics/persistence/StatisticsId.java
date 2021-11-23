package de.cotto.lndmanagej.statistics.persistence;

import javax.annotation.Nullable;
import java.io.Serial;
import java.io.Serializable;

@SuppressWarnings("unused")
public final class StatisticsId implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    @Nullable
    private Long channelId;

    private long timestamp;

    public StatisticsId() {
        // for JPA
    }
}