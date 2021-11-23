package de.cotto.lndmanagej.statistics.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.statistics.Balances;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.time.ZoneOffset;
import java.util.Objects;

@Entity
@IdClass(BalancesId.class)
@Table(name = "balances")
class BalancesJpaDto {
    @Id
    private long timestamp;

    @Id
    @Nullable
    private Long channelId;

    private long localBalance;
    private long localReserved;
    private long remoteBalance;
    private long remoteReserved;

    BalancesJpaDto() {
        // for JPA
    }

    protected static BalancesJpaDto fromModel(Balances balances) {
        BalancesJpaDto dto = new BalancesJpaDto();
        dto.timestamp = balances.timestamp().toEpochSecond(ZoneOffset.UTC);
        dto.channelId = balances.channelId().getShortChannelId();
        dto.localBalance = balances.balanceInformation().localBalance().satoshis();
        dto.localReserved = balances.balanceInformation().localReserve().satoshis();
        dto.remoteBalance = balances.balanceInformation().remoteBalance().satoshis();
        dto.remoteReserved = balances.balanceInformation().remoteReserve().satoshis();
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
