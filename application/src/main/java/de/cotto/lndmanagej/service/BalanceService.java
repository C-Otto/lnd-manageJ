package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.BalanceInformation;
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

    public Coins getAvailableLocalBalance(ChannelId channelId) {
        return grpcChannels.getChannel(channelId)
                .map(LocalChannel::getBalanceInformation)
                .map(BalanceInformation::availableLocalBalance)
                .orElse(Coins.NONE);
    }

    public Coins getAvailableRemoteBalance(ChannelId channelId) {
        return grpcChannels.getChannel(channelId)
                .map(LocalChannel::getBalanceInformation)
                .map(BalanceInformation::availableRemoteBalance)
                .orElse(Coins.NONE);
    }
}
