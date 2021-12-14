package de.cotto.lndmanagej.dto;

import de.cotto.lndmanagej.controller.dto.SelfPaymentDto;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SelfPayment;

import java.util.List;
import java.util.function.Function;

public record SelfPaymentsDto(List<SelfPaymentDto> selfPayments, String amountPaid, String fees) {
    public SelfPaymentsDto(List<SelfPayment> selfPayments) {
        this(
                selfPayments.stream().map(SelfPaymentDto::createFromModel).toList(),
                sumToString(selfPayments, SelfPayment::amountPaid),
                sumToString(selfPayments, SelfPayment::fees)
        );
    }

    private static String sumToString(List<SelfPayment> selfPayments, Function<SelfPayment, Coins> coinFunction) {
        return String.valueOf(sum(selfPayments, coinFunction).milliSatoshis());
    }

    private static Coins sum(List<SelfPayment> selfPayments, Function<SelfPayment, Coins> coinFunction) {
        return selfPayments.stream()
                .map(coinFunction)
                .reduce(Coins.NONE, Coins::add);
    }
}
