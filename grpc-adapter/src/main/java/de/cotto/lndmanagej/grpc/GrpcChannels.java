package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.CloseType;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.CoopClosedChannel;
import de.cotto.lndmanagej.model.ForceClosedChannel;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.WaitingCloseChannel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelCloseSummary.ClosureType;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.PendingChannel;
import lnrpc.PendingHTLC;
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

    public Set<WaitingCloseChannel> getWaitingCloseChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getWaitingCloseChannels().stream()
                .map(waitingCloseChannel -> toWaitingCloseChannel(waitingCloseChannel, ownPubkey))
                .flatMap(Optional::stream)
                .collect(toSet());
    }

    private Optional<WaitingCloseChannel> toWaitingCloseChannel(
            PendingChannelsResponse.WaitingCloseChannel waitingCloseChannel,
            Pubkey ownPubkey
    ) {
        PendingChannel pendingChannel = waitingCloseChannel.getChannel();
        ChannelPoint channelPoint = ChannelPoint.create(pendingChannel.getChannelPoint());
        return channelIdResolver.resolveFromChannelPoint(channelPoint)
                .map(id -> new WaitingCloseChannel(
                        id,
                        channelPoint,
                        Coins.ofSatoshis(pendingChannel.getCapacity()),
                        ownPubkey,
                        Pubkey.create(pendingChannel.getRemoteNodePub())
                ));
    }

    private Optional<ForceClosingChannel> toForceClosingChannel(
            PendingChannelsResponse.ForceClosedChannel forceClosedChannel,
            Pubkey ownPubkey
    ) {
        PendingChannel pendingChannel = forceClosedChannel.getChannel();
        ChannelPoint channelPoint = ChannelPoint.create(pendingChannel.getChannelPoint());
        return channelIdResolver.resolveFromChannelPoint(channelPoint)
                .map(id -> new ForceClosingChannel(
                        id,
                        channelPoint,
                        Coins.ofSatoshis(pendingChannel.getCapacity()),
                        ownPubkey,
                        Pubkey.create(pendingChannel.getRemoteNodePub()),
                        forceClosedChannel.getClosingTxid(),
                        getHtlcOutpoints(forceClosedChannel)
                ));
    }

    private Set<ChannelPoint> getHtlcOutpoints(PendingChannelsResponse.ForceClosedChannel forceClosedChannel) {
        return forceClosedChannel.getPendingHtlcsList().stream()
                .map(PendingHTLC::getOutpoint)
                .map(ChannelPoint::create)
                .collect(toSet());
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
        Optional<ChannelId> optionalChannelId = getChannelId(channelCloseSummary)
                .or(() -> channelIdResolver.resolveFromChannelPoint(channelPoint));
        ClosureType closureType = channelCloseSummary.getCloseType();
        if (closureType.equals(ClosureType.COOPERATIVE_CLOSE)) {
            return optionalChannelId
                    .map(id -> new CoopClosedChannel(
                            id,
                            channelPoint,
                            capacity,
                            ownPubkey,
                            remotePubkey,
                            channelCloseSummary.getClosingTxHash()
                    ));
        }
        return optionalChannelId
                .map(id -> new ForceClosedChannel(
                        id,
                        channelPoint,
                        capacity,
                        ownPubkey,
                        remotePubkey,
                        channelCloseSummary.getClosingTxHash(),
                        getCloseType(closureType)
                ));
    }

    private CloseType getCloseType(ClosureType closureType) {
        CloseType closeType;
        if (closureType.equals(ClosureType.REMOTE_FORCE_CLOSE)) {
            closeType = CloseType.REMOTE;
        } else if (closureType.equals(ClosureType.LOCAL_FORCE_CLOSE)) {
            closeType = CloseType.LOCAL;
        } else if  (closureType.equals(ClosureType.BREACH_CLOSE)) {
            closeType = CloseType.BREACH;
        } else {
            throw new IllegalStateException("unexpected close type: " + closureType);
        }
        return closeType;
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
