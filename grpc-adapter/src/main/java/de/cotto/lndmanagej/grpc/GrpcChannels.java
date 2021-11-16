package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelCloseSummary.ClosureType;
import lnrpc.PendingChannelsResponse.ForceClosedChannel;
import lnrpc.PendingChannelsResponse.PendingChannel;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class GrpcChannels {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;
    private final ChannelIdResolver channelIdResolver;

    public GrpcChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo,
            ChannelIdResolver channelIdResolver
    ) {
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
        this.channelIdResolver = channelIdResolver;
    }

    public Set<LocalOpenChannel> getChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getChannels().stream()
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .collect(toSet());
    }

    public Set<ClosedChannel> getClosedChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getClosedChannels().stream()
                .filter(this::hasSupportedCloseType)
                .map(channelCloseSummary -> toClosedChannel(channelCloseSummary, ownPubkey))
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    public Set<ForceClosingChannel> getForceClosingChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getForceClosingChannels().stream()
                .map(forceClosedChannel -> toForceClosingChannel(forceClosedChannel, ownPubkey))
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    private Optional<ForceClosingChannel> toForceClosingChannel(
            ForceClosedChannel forceClosedChannel,
            Pubkey ownPubkey
    ) {
        PendingChannel pendingChannel = forceClosedChannel.getChannel();
        ChannelPoint channelPoint = ChannelPoint.create(pendingChannel.getChannelPoint());
        Coins capacity = Coins.ofSatoshis(pendingChannel.getCapacity());
        Pubkey remotePubkey = Pubkey.create(pendingChannel.getRemoteNodePub());
        return channelIdResolver.resolveFromChannelPoint(channelPoint)
                .map(id -> new ForceClosingChannel(id, channelPoint, capacity, ownPubkey, remotePubkey));
    }

    public Optional<LocalOpenChannel> getChannel(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        long expectedChannelId = channelId.getShortChannelId();
        return grpcService.getChannels().stream()
                .filter(c -> c.getChanId() == expectedChannelId)
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .findFirst();
    }

    private LocalOpenChannel toLocalOpenChannel(lnrpc.Channel lndChannel, Pubkey ownPubkey) {
        ChannelId channelId = ChannelId.fromShortChannelId(lndChannel.getChanId());
        ChannelPoint channelPoint = ChannelPoint.create(lndChannel.getChannelPoint());
        Pubkey remotePubkey = Pubkey.create(lndChannel.getRemotePubkey());
        Coins capacity = Coins.ofSatoshis(lndChannel.getCapacity());
        BalanceInformation balanceInformation = new BalanceInformation(
                Coins.ofSatoshis(lndChannel.getLocalBalance()),
                Coins.ofSatoshis(lndChannel.getLocalConstraints().getChanReserveSat()),
                Coins.ofSatoshis(lndChannel.getRemoteBalance()),
                Coins.ofSatoshis(lndChannel.getRemoteConstraints().getChanReserveSat())
        );
        return new LocalOpenChannel(channelId, channelPoint, capacity, ownPubkey, remotePubkey, balanceInformation);
    }

    private Optional<ClosedChannel> toClosedChannel(
            ChannelCloseSummary channelCloseSummary,
            Pubkey ownPubkey
    ) {
        ChannelPoint channelPoint = ChannelPoint.create(channelCloseSummary.getChannelPoint());
        Pubkey remotePubkey = Pubkey.create(channelCloseSummary.getRemotePubkey());
        Coins capacity = Coins.ofSatoshis(channelCloseSummary.getCapacity());
        return getChannelId(channelCloseSummary)
                .or(() -> channelIdResolver.resolveFromChannelPoint(channelPoint))
                .map(id -> new ClosedChannel(id, channelPoint, capacity, ownPubkey, remotePubkey));
    }

    private Optional<ChannelId> getChannelId(ChannelCloseSummary channelCloseSummary) {
        long chanId = channelCloseSummary.getChanId();
        if (chanId == 0) {
            return Optional.empty();
        }
        return Optional.of(ChannelId.fromShortChannelId(chanId));
    }

    private boolean hasSupportedCloseType(ChannelCloseSummary channelCloseSummary) {
        ClosureType closeType = channelCloseSummary.getCloseType();
        return closeType != ClosureType.ABANDONED && closeType != ClosureType.FUNDING_CANCELED;
    }
}
