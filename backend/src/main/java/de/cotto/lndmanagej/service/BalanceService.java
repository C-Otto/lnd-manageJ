package de.cotto.lndmanagej.service;

import com.codahale.metrics.annotation.Timed;
import de.cotto.lndmanagej.balances.BalancesDao;
import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoinsAndDuration;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BalanceService {
    private final GrpcChannels grpcChannels;
    private final ChannelService channelService;
    private final BalancesDao balancesDao;

    public BalanceService(
            GrpcChannels grpcChannels,
            ChannelService channelService,
            BalancesDao balancesDao
    ) {
        this.grpcChannels = grpcChannels;
        this.channelService = channelService;
        this.balancesDao = balancesDao;
    }

    @Timed
    public Coins getAvailableLocalBalanceForPeer(Pubkey peer) {
        return channelService.getOpenChannelsWith(peer).stream()
                .map(LocalOpenChannel::getId)
                .map(this::getAvailableLocalBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getAvailableLocalBalance(ChannelId channelId) {
        return getBalanceInformation(channelId)
                .map(BalanceInformation::localAvailable)
                .orElse(Coins.NONE);
    }

    @Timed
    public Coins getAvailableRemoteBalanceForPeer(Pubkey peer) {
        return channelService.getOpenChannelsWith(peer).stream()
                .map(LocalOpenChannel::getId)
                .map(this::getAvailableRemoteBalance)
                .reduce(Coins.NONE, Coins::add);
    }

    @Timed
    public Coins getAvailableRemoteBalance(ChannelId channelId) {
        return getBalanceInformation(channelId)
                .map(BalanceInformation::remoteAvailable)
                .orElse(Coins.NONE);
    }

    @Timed
    public BalanceInformation getBalanceInformationForPeer(Pubkey pubkey) {
        return channelService.getOpenChannelsWith(pubkey).parallelStream()
                .map(Channel::getId)
                .map(this::getBalanceInformation)
                .flatMap(Optional::stream)
                .reduce(BalanceInformation.EMPTY, BalanceInformation::add);
    }

    @Timed
    public Optional<BalanceInformation> getBalanceInformation(ChannelId channelId) {
        return grpcChannels.getChannel(channelId)
                .map(LocalOpenChannel::getBalanceInformation);
    }

    @Timed
    public Optional<Coins> getLocalBalanceMinimum(ChannelId channelId, int days) {
        return balancesDao.getLocalBalanceMinimum(channelId, days);
    }

    @Timed
    public Optional<Coins> getLocalBalanceMaximum(ChannelId channelId, int days) {
        return balancesDao.getLocalBalanceMaximum(channelId, days);
    }

    @Timed
    public Optional<CoinsAndDuration> getLocalBalanceAverage(ChannelId channelId, int days) {
        if (channelService.isClosed(channelId)) {
            return balancesDao.getLocalBalanceAverageClosedChannel(channelId, days);
        }
        return balancesDao.getLocalBalanceAverageOpenChannel(channelId, days);
    }
}
