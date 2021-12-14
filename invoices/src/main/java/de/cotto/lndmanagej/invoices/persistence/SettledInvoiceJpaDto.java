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
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

@Entity
@Table(
        name = "settled_invoices",
        indexes = {
            @Index(unique = true, columnList = "settleIndex"),
            @Index(columnList = "hash"),
            @Index(columnList = "settleDate")
        })
public class SettledInvoiceJpaDto {
    private static final int KEYSEND_MESSAGE_LENGTH = 5_000;

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
    @Column(length = KEYSEND_MESSAGE_LENGTH)
    private String keysendMessage;
    private long receivedVia;

    public SettledInvoiceJpaDto() {
        // for JPA
    }

    public static SettledInvoiceJpaDto createFromModel(SettledInvoice settledInvoice) {
        SettledInvoiceJpaDto jpaDto = new SettledInvoiceJpaDto();
        jpaDto.addIndex = settledInvoice.addIndex();
        jpaDto.settleIndex = settledInvoice.settleIndex();
        jpaDto.settleDate = settledInvoice.settleDate().toEpochSecond();
        jpaDto.hash = settledInvoice.hash();
        jpaDto.amountPaid = settledInvoice.amountPaid().milliSatoshis();
        jpaDto.memo = settledInvoice.memo();
        jpaDto.keysendMessage = getTruncatedKeysendMessage(settledInvoice);
        jpaDto.receivedVia = settledInvoice.receivedVia().map(ChannelId::getShortChannelId).orElse(0L);
        return jpaDto;
    }

    public SettledInvoice toModel() {
        ZonedDateTime dateTime = LocalDateTime.ofEpochSecond(settleDate, 0, UTC).atZone(UTC);
        Optional<ChannelId> channelId;
        if (receivedVia > 0) {
            channelId = Optional.of(ChannelId.fromShortChannelId(receivedVia));
        } else {
            channelId = Optional.empty();
        }
        return new SettledInvoice(
                addIndex,
                settleIndex,
                dateTime,
                Objects.requireNonNull(hash),
                Coins.ofMilliSatoshis(amountPaid),
                Objects.requireNonNull(memo),
                Optional.ofNullable(keysendMessage),
                channelId
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

    public long getReceivedVia() {
        return receivedVia;
    }

    @Nullable
    private static String getTruncatedKeysendMessage(SettledInvoice settledInvoice) {
        String message = settledInvoice.keysendMessage().orElse(null);
        if (message == null || message.length() <= KEYSEND_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, KEYSEND_MESSAGE_LENGTH);
    }
}
