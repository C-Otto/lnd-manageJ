package de.cotto.lndmanagej.balances.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.balances.Balances;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@IdClass(BalancesId.class)
@Table(name = "balances")
public class BalancesJpaDto {
    @Id
    private long timestamp;

    @Id
    private long channelId;

    private long localBalance;
    private long localReserved;
    private long remoteBalance;
    private long remoteReserved;

    public BalancesJpaDto() {
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

    public Balances toModel() {
        BalanceInformation balanceInformation = new BalanceInformation(
                Coins.ofSatoshis(localBalance),
                Coins.ofSatoshis(localReserved),
                Coins.ofSatoshis(remoteBalance),
                Coins.ofSatoshis(remoteReserved)
        );
        LocalDateTime timestamp = LocalDateTime.ofEpochSecond(this.timestamp, 0, ZoneOffset.UTC);
        return new Balances(timestamp, ChannelId.fromShortChannelId(channelId), balanceInformation);
    }

    @VisibleForTesting
    protected long getTimestamp() {
        return timestamp;
    }

    @VisibleForTesting
    protected long getChannelId() {
        return channelId;
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
