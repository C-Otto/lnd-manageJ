package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import org.springframework.stereotype.Component;

@Component
public class BalanceService {
    private final GrpcChannels grpcChannels;

    public BalanceService(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
    }

    public Coins getLocalBalance(ChannelId channelId) {
        return grpcChannels.getChannel(channelId).map(LocalChannel::getLocalBalance).orElse(Coins.NONE);
    }

    public Coins getLocalReserve(ChannelId channelId) {
        return grpcChannels.getChannel(channelId).map(LocalChannel::getLocalReserve).orElse(Coins.NONE);
    }

    public Coins getAvailableLocalBalance(ChannelId channelId) {
        Coins available = grpcChannels.getChannel(channelId)
                .map(c -> c.getLocalBalance().subtract(c.getLocalReserve()))
                .orElse(Coins.NONE);
        if (available.isNegative()) {
            return Coins.NONE;
        }
        return available;
    }
}
