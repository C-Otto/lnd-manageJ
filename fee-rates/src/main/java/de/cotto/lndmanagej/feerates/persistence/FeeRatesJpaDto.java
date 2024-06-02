package de.cotto.lndmanagej.feerates.persistence;

import com.google.common.annotations.VisibleForTesting;
import de.cotto.lndmanagej.feerates.FeeRates;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeRateInformation;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@IdClass(FeeRatesId.class)
@Table(name = "feerates")
public class FeeRatesJpaDto {
    @Id
    private long timestamp;

    @Id
    private long channelId;

    private long baseFeeLocal;
    private long feeRateLocal;
    private long inboundFeeRateLocal;
    private long inboundBaseFeeLocal;

    private long baseFeeRemote;
    private long feeRateRemote;
    private long inboundFeeRateRemote;
    private long inboundBaseFeeRemote;

    public FeeRatesJpaDto() {
        // for JPA
    }

    protected static FeeRatesJpaDto fromModel(FeeRates feeRates) {
        FeeRatesJpaDto dto = new FeeRatesJpaDto();
        dto.timestamp = feeRates.timestamp().toEpochSecond(ZoneOffset.UTC);
        dto.channelId = feeRates.channelId().getShortChannelId();

        dto.baseFeeLocal = feeRates.feeRates().baseFeeLocal().milliSatoshis();
        dto.feeRateLocal = feeRates.feeRates().feeRateLocal();
        dto.inboundBaseFeeLocal = feeRates.feeRates().inboundBaseFeeLocal().milliSatoshis();
        dto.inboundFeeRateLocal = feeRates.feeRates().inboundFeeRateLocal();

        dto.baseFeeRemote = feeRates.feeRates().baseFeeRemote().milliSatoshis();
        dto.feeRateRemote = feeRates.feeRates().feeRateRemote();
        dto.inboundBaseFeeRemote = feeRates.feeRates().inboundBaseFeeRemote().milliSatoshis();
        dto.inboundFeeRateRemote = feeRates.feeRates().inboundFeeRateRemote();

        return dto;
    }

    public FeeRates toModel() {
        FeeRateInformation feeRates = new FeeRateInformation(
                Coins.ofMilliSatoshis(baseFeeLocal),
                feeRateLocal,
                Coins.ofMilliSatoshis(inboundBaseFeeLocal),
                inboundFeeRateLocal,
                Coins.ofMilliSatoshis(baseFeeRemote),
                feeRateRemote,
                Coins.ofMilliSatoshis(inboundBaseFeeRemote),
                inboundFeeRateRemote
        );
        LocalDateTime timestamp = LocalDateTime.ofEpochSecond(this.timestamp, 0, ZoneOffset.UTC);
        return new FeeRates(timestamp, ChannelId.fromShortChannelId(channelId), feeRates);
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
    public long getBaseFeeLocal() {
        return baseFeeLocal;
    }

    @VisibleForTesting
    public long getFeeRateLocal() {
        return feeRateLocal;
    }

    @VisibleForTesting
    public long getInboundFeeRateLocal() {
        return inboundFeeRateLocal;
    }

    @VisibleForTesting
    public long getInboundBaseFeeLocal() {
        return inboundBaseFeeLocal;
    }

    @VisibleForTesting
    public long getBaseFeeRemote() {
        return baseFeeRemote;
    }

    @VisibleForTesting
    public long getFeeRateRemote() {
        return feeRateRemote;
    }

    @VisibleForTesting
    public long getInboundFeeRateRemote() {
        return inboundFeeRateRemote;
    }

    @VisibleForTesting
    public long getInboundBaseFeeRemote() {
        return inboundBaseFeeRemote;
    }
}
