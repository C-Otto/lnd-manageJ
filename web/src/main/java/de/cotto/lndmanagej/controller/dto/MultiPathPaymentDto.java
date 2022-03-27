package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;
import de.cotto.lndmanagej.pickhardtpayments.model.Route;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record MultiPathPaymentDto(
        String amountSat,
        double probability,
        String feesMilliSat,
        long feeRate,
        List<RouteDto> routes
) {
    public static MultiPathPaymentDto fromModel(MultiPathPayment multiPathPayment) {
        return new MultiPathPaymentDto(
                String.valueOf(multiPathPayment.amount().satoshis()),
                multiPathPayment.probability(),
                String.valueOf(multiPathPayment.fees().milliSatoshis()),
                multiPathPayment.getFeeRate(),
                getRoutes(multiPathPayment.routes())
        );
    }

    private static List<RouteDto> getRoutes(Set<Route> routes) {
        return routes.stream().map(RouteDto::fromModel).collect(Collectors.toList());
    }
}
