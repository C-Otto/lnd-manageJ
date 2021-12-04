package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "settled_invoices", indexes = {@Index(unique = true, columnList = "settleIndex")})
class SettledInvoiceJpaDto {
    @Id
    private long addIndex;

    private long settleIndex;

    private long settleDate;

    @Nullable
    private String hash;
    private long amountPaid;

    @Nullable
    private String memo;

    @Nullable
    @Column(length = 5_000)
    private String keysendMessage;
    private long receivedVia;

    public SettledInvoiceJpaDto() {
        // for JPA
    }

    public static SettledInvoiceJpaDto createFromInvoice(SettledInvoice settledInvoice) {
        SettledInvoiceJpaDto jpaDto = new SettledInvoiceJpaDto();
        jpaDto.addIndex = settledInvoice.addIndex();
        jpaDto.settleIndex = settledInvoice.settleIndex();
        jpaDto.settleDate = settledInvoice.settleDate().toEpochSecond(ZoneOffset.UTC);
        jpaDto.hash = settledInvoice.hash();
        jpaDto.amountPaid = settledInvoice.amountPaid().milliSatoshis();
        jpaDto.memo = settledInvoice.memo();
        jpaDto.keysendMessage = settledInvoice.keysendMessage().orElse(null);
        jpaDto.receivedVia = settledInvoice.receivedVia().getShortChannelId();
        return jpaDto;
    }

    public SettledInvoice toModel() {
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(settleDate, 0, ZoneOffset.UTC);
        return new SettledInvoice(
                addIndex,
                settleIndex,
                localDateTime,
                Objects.requireNonNull(hash),
                Coins.ofMilliSatoshis(amountPaid),
                Objects.requireNonNull(memo),
                Optional.ofNullable(keysendMessage),
                ChannelId.fromShortChannelId(receivedVia)
        );
    }

    public long getAddIndex() {
        return addIndex;
    }

    public long getSettleIndex() {
        return settleIndex;
    }

    public long getSettleDate() {
        return settleDate;
    }

    public String getHash() {
        return Objects.requireNonNull(hash);
    }

    public long getAmountPaid() {
        return amountPaid;
    }

    public String getMemo() {
        return Objects.requireNonNull(memo);
    }

    @Nullable
    public String getKeysendMessage() {
        return keysendMessage;
    }
}
