package de.cotto.lndmanagej.invoices.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SettledInvoice;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toMap;

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

    @Nullable
    @ElementCollection
    @CollectionTable(name = "settled_invoice_received_via")
    private List<ReceivedViaJpaDto> receivedVia;

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
        jpaDto.receivedVia = settledInvoice.receivedVia().entrySet().stream()
                .map(entry -> new ReceivedViaJpaDto(
                        entry.getKey().getShortChannelId(),
                        entry.getValue().milliSatoshis())
                )
                .toList();
        return jpaDto;
    }

    public SettledInvoice toModel() {
        ZonedDateTime dateTime = LocalDateTime.ofEpochSecond(settleDate, 0, UTC).atZone(UTC);
        return new SettledInvoice(
                addIndex,
                settleIndex,
                dateTime,
                Objects.requireNonNull(hash),
                Coins.ofMilliSatoshis(amountPaid),
                Objects.requireNonNull(memo),
                Optional.ofNullable(keysendMessage),
                getReceivedVia()
        );
    }

    private Map<ChannelId, Coins> getReceivedVia() {
        return Objects.requireNonNull(receivedVia).stream().collect(toMap(
                entry -> ChannelId.fromShortChannelId(entry.getChannelId()),
                entry -> Coins.ofMilliSatoshis(entry.getAmount())
        ));
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

    @Nullable
    private static String getTruncatedKeysendMessage(SettledInvoice settledInvoice) {
        String message = settledInvoice.keysendMessage().orElse(null);
        if (message == null || message.length() <= KEYSEND_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, KEYSEND_MESSAGE_LENGTH);
    }
}
