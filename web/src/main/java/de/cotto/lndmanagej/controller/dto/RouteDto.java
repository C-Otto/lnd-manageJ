package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Edge;
import de.cotto.lndmanagej.model.Route;

import java.util.List;

public record RouteDto(
        String amountSat,
        List<ChannelId> channelIds,
        double probability,
        String feesMilliSat,
        String feesWithFirstHopMilliSat,
        long feeRate,
        long feeRateWithFirstHop
) {
    public static RouteDto fromModel(Route route) {
        return new RouteDto(
                String.valueOf(route.getAmount().satoshis()),
                route.getEdges().stream().map(Edge::channelId).toList(),
                route.getProbability(),
                String.valueOf(route.getFees().milliSatoshis()),
                String.valueOf(route.getFeesWithFirstHop().milliSatoshis()),
                route.getFeeRate(),
                route.getFeeRateWithFirstHop()
        );
    }

}
