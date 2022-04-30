package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.Route;
import de.cotto.lndmanagej.pickhardtpayments.model.MultiPathPayment;

import java.util.List;

public record MultiPathPaymentDto(
        String amountSat,
        double probability,
        String feesMilliSat,
        String feesWithFirstHopMilliSat,
        long feeRate,
        long feeRateWithFirstHop,
        List<RouteDto> routes
) {
    public static MultiPathPaymentDto fromModel(MultiPathPayment multiPathPayment) {
        return new MultiPathPaymentDto(
                String.valueOf(multiPathPayment.amount().satoshis()),
                multiPathPayment.probability(),
                String.valueOf(multiPathPayment.fees().milliSatoshis()),
                String.valueOf(multiPathPayment.feesWithFirstHop().milliSatoshis()),
                multiPathPayment.getFeeRate(),
                multiPathPayment.getFeeRateWithFirstHop(),
                getRoutes(multiPathPayment.routes())
        );
    }

    private static List<RouteDto> getRoutes(List<Route> routes) {
        return routes.stream().map(RouteDto::fromModel).toList();
    }
}
