package de.cotto.lndmanagej.selfpayments.persistence;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SelfPayment;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static java.time.ZoneOffset.UTC;

public record SelfPaymentJpaDto(
        String memo,
        long settleDate,
        long amountPaid,
        long fees,
        long firstChannel,
        long receivedVia
) {
    public SelfPayment toModel() {
        ZonedDateTime dateTime = LocalDateTime.ofEpochSecond(settleDate, 0, UTC).atZone(UTC);
        return new SelfPayment(
                memo,
                dateTime,
                Coins.ofMilliSatoshis(amountPaid),
                Coins.ofMilliSatoshis(fees),
                toOptionalChannelId(firstChannel),
                toOptionalChannelId(receivedVia)
        );
    }

    private Optional<ChannelId> toOptionalChannelId(long channelId) {
        if (channelId > 0) {
            return Optional.of(ChannelId.fromShortChannelId(channelId));
        }
        return Optional.empty();
    }
}
