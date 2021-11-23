package de.cotto.lndmanagej.statistics.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.statistics.Statistics;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@IdClass(StatisticsId.class)
@Table(name = "statistics")
class StatisticsJpaDto {
    @Id
    private long timestamp;

    @Id
    @Nullable
    private Long channelId;

    private long localBalance;
    private long localReserved;
    private long remoteBalance;
    private long remoteReserved;

    StatisticsJpaDto() {
        // for JPA
    }

    protected static StatisticsJpaDto fromModel(Statistics statistics) {
        StatisticsJpaDto dto = new StatisticsJpaDto();
        dto.timestamp = statistics.timestamp().toEpochSecond(ZoneOffset.UTC);
        dto.channelId = statistics.channelId().getShortChannelId();
        dto.localBalance = statistics.balanceInformation().localBalance().satoshis();
        dto.localReserved = statistics.balanceInformation().localReserve().satoshis();
        dto.remoteBalance = statistics.balanceInformation().remoteBalance().satoshis();
        dto.remoteReserved = statistics.balanceInformation().remoteReserve().satoshis();
        return dto;
    }

    @VisibleForTesting
    protected long getTimestamp() {
        return timestamp;
    }

    @VisibleForTesting
    protected long getChannelId() {
        return Objects.requireNonNull(channelId);
    }

    @VisibleForTesting
    protected long getLocalBalance() {
        return localBalance;
    }

    @VisibleForTesting
    protected long getLocalReserved() {
        return localReserved;
    }

    @VisibleForTesting
    protected long getRemoteBalance() {
        return remoteBalance;
    }

    @VisibleForTesting
    protected long getRemoteReserved() {
        return remoteReserved;
    }
}
