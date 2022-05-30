package de.cotto.lndmanagej.model;

import java.util.List;
import java.util.Optional;

public record SelfPaymentRoute(ChannelId channelIdOut, Coins amount, ChannelId channelIdIn) {
    public static Optional<SelfPaymentRoute> create(PaymentRoute paymentRoute) {
        List<PaymentHop> hops = paymentRoute.hops();
        if (hops.isEmpty()) {
            return Optional.empty();
        }
        PaymentHop firstHop = hops.get(0);
        PaymentHop lastHop = hops.get(hops.size() - 1);
        return Optional.of(new SelfPaymentRoute(firstHop.channelId(), lastHop.amount(), lastHop.channelId()));
    }
}
