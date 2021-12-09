package de.cotto.lndmanagej.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record Payment(
        long index,
        String paymentHash,
        LocalDateTime creationDateTime,
        Coins value,
        Coins fees,
        List<PaymentRoute> routes
) {
    public Optional<ChannelId> getFirstChannel() {
        return routes.stream().flatMap(route -> route.hops().stream()).map(PaymentHop::channelId).findFirst();
    }
}
