package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.SelfPayment;

import java.time.ZonedDateTime;
import java.util.List;

public record SelfPaymentDto(
        ZonedDateTime settleDate,
        String memo,
        String amountPaidMilliSat,
        String feesMilliSat,
        List<SelfPaymentRouteDto> routes
) {
    public static SelfPaymentDto createFromModel(SelfPayment selfPayment) {
        return new SelfPaymentDto(
                selfPayment.settleDate(),
                selfPayment.memo(),
                String.valueOf(selfPayment.amountPaid().milliSatoshis()),
                String.valueOf(selfPayment.fees().milliSatoshis()),
                selfPayment.routes().stream().map(SelfPaymentRouteDto::fromModel).toList()
        );
    }
}
