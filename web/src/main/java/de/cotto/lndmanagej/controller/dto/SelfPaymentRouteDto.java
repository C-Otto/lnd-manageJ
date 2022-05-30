package de.cotto.lndmanagej.controller.dto;

import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.SelfPaymentRoute;

public record SelfPaymentRouteDto(ChannelId channelIdOut, String amountMilliSat, ChannelId channelIdIn) {
    public SelfPaymentRouteDto(ChannelId channelIdOut, Coins amount, ChannelId channelIdIn) {
        this(channelIdOut, String.valueOf(amount.milliSatoshis()), channelIdIn);
    }

    public static SelfPaymentRouteDto fromModel(SelfPaymentRoute selfPaymentRoute) {
        return new SelfPaymentRouteDto(
                selfPaymentRoute.channelIdOut(),
                selfPaymentRoute.amount(),
                selfPaymentRoute.channelIdIn()
        );
    }
}
