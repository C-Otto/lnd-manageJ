package de.cotto.lndmanagej.model;

import java.util.Optional;

public record SelfPaymentRoute(ChannelId channelIdOut, Coins amount, ChannelId channelIdIn) {
    public static Optional<SelfPaymentRoute> create(PaymentRoute paymentRoute) {
        Optional<PaymentHop> firstHop = paymentRoute.firstHop();
        Optional<PaymentHop> lastHop = paymentRoute.lastHop();
        if (firstHop.isEmpty() || lastHop.isEmpty()) {
            return Optional.empty();
        }
        SelfPaymentRoute selfPaymentRoute =
                new SelfPaymentRoute(firstHop.get().channelId(), lastHop.get().amount(), lastHop.get().channelId());
        return Optional.of(selfPaymentRoute);
    }
}
