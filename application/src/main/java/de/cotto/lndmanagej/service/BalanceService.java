package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BalanceService {
    private final GrpcChannels grpcChannels;
    private final ChannelService channelService;

    public BalanceService(GrpcChannels grpcChannels, ChannelService channelService) {
        this.grpcChannels = grpcChannels;
        this.channelService = channelService;
    }

    public Coins getAvailableLocalBalance(Pubkey peer) {
        return channelService.getOpenChannelsWith(peer).stream()
                .map(LocalOpenChannel::getId)
                .map(this::getAvailableLocalBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getAvailableLocalBalance(ChannelId channelId) {
        return getBalanceInformation(channelId)
                .map(BalanceInformation::availableLocalBalance)
                .orElse(Coins.NONE);
    }

    public Coins getAvailableRemoteBalance(Pubkey peer) {
        return channelService.getOpenChannelsWith(peer).stream()
                .map(LocalOpenChannel::getId)
                .map(this::getAvailableRemoteBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    public Coins getAvailableRemoteBalance(ChannelId channelId) {
        return getBalanceInformation(channelId)
                .map(BalanceInformation::availableRemoteBalance)
                .orElse(Coins.NONE);
    }

    private Optional<BalanceInformation> getBalanceInformation(ChannelId channelId) {
        return grpcChannels.getChannel(channelId)
                .map(LocalOpenChannel::getBalanceInformation);
    }
}
