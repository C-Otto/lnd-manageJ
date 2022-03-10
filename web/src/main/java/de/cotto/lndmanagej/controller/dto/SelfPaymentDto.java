package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.SelfPayment;

import java.time.ZonedDateTime;

public record SelfPaymentDto(
        ZonedDateTime settleDate,
        String memo,
        String amountPaidMilliSat,
        String feesMilliSat,
        ChannelId firstChannel,
        ChannelId lastChannel
) {
    public static SelfPaymentDto createFromModel(SelfPayment selfPayment) {
        return new SelfPaymentDto(
                selfPayment.settleDate(),
                selfPayment.memo(),
                String.valueOf(selfPayment.amountPaid().milliSatoshis()),
                String.valueOf(selfPayment.fees().milliSatoshis()),
                selfPayment.firstChannel().orElseThrow(),
                selfPayment.lastChannel().orElseThrow()
        );
    }
}
