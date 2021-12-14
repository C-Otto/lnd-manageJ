package de.cotto.lndmanagej.model;

import java.time.ZonedDateTime;
import java.util.Optional;

public record SelfPayment(
        String memo,
        ZonedDateTime settleDate,
        Coins amountPaid,
        Coins fees,
        Optional<ChannelId> firstChannel,
        Optional<ChannelId> lastChannel
) {
    public SelfPayment(Payment payment, SettledInvoice settledInvoice) {
        this(
                settledInvoice.memo(),
                settledInvoice.settleDate(),
                settledInvoice.amountPaid(),
                payment.fees(),
                payment.getFirstChannel(),
                settledInvoice.receivedVia()
        );
    }
}
