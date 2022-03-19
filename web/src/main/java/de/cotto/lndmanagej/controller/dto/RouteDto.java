package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.pickhardtpayments.model.Edge;
import de.cotto.lndmanagej.pickhardtpayments.model.Route;

import java.util.List;

public record RouteDto(String amountSat, List<ChannelId> channelIds, double probability) {
    public static RouteDto fromModel(Route route) {
        return new RouteDto(
                String.valueOf(route.amount().satoshis()),
                route.edges().stream().map(Edge::channelId).toList(),
                route.getProbability()
        );
    }

}
