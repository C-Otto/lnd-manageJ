package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.ChannelCoreInformation;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.ForceClosingChannel;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.OpenInitiator;
import de.cotto.lndmanagej.model.PendingOpenChannel;
import de.cotto.lndmanagej.model.PrivateResolver;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.TransactionHash;
import de.cotto.lndmanagej.model.WaitingCloseChannel;
import lnrpc.Channel;
import lnrpc.PendingChannelsResponse;
import lnrpc.PendingChannelsResponse.PendingChannel;
import lnrpc.PendingHTLC;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class GrpcChannels extends GrpcChannelsBase {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;

    public GrpcChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo,
            ChannelIdResolver channelIdResolver,
            PrivateResolver privateResolver
    ) {
        super(channelIdResolver, privateResolver);
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
    }

    public Set<LocalOpenChannel> getChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getChannels().stream()
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .collect(toSet());
    }

    public Set<PendingOpenChannel> getPendingOpenChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getPendingOpenChannels().stream()
                .map(pendingOpenChannel -> toPendingOpenChannel(pendingOpenChannel.getChannel(), ownPubkey))
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

    public Optional<LocalOpenChannel> getChannel(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        long expectedChannelId = channelId.getShortChannelId();
        return grpcService.getChannels().stream()
                .filter(c -> c.getChanId() == expectedChannelId)
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .findFirst();
    }

    private Optional<WaitingCloseChannel> toWaitingCloseChannel(
            PendingChannelsResponse.WaitingCloseChannel waitingCloseChannel,
            Pubkey ownPubkey
    ) {
        PendingChannel pendingChannel = waitingCloseChannel.getChannel();
        ChannelPoint channelPoint = ChannelPoint.create(pendingChannel.getChannelPoint());
        return resolveChannelId(channelPoint).map(id -> new WaitingCloseChannel(
                new ChannelCoreInformation(id, channelPoint, Coins.ofSatoshis(pendingChannel.getCapacity())), ownPubkey,
                Pubkey.create(pendingChannel.getRemoteNodePub()),
                getOpenInitiator(pendingChannel.getInitiator()),
                resolveIsPrivate(id)
        ));
    }

    private Optional<ForceClosingChannel> toForceClosingChannel(
            PendingChannelsResponse.ForceClosedChannel forceClosedChannel,
            Pubkey ownPubkey
    ) {
        PendingChannel pendingChannel = forceClosedChannel.getChannel();
        ChannelPoint channelPoint = ChannelPoint.create(pendingChannel.getChannelPoint());
        return resolveChannelId(channelPoint).map(id -> new ForceClosingChannel(
                new ChannelCoreInformation(id, channelPoint, Coins.ofSatoshis(pendingChannel.getCapacity())),
                ownPubkey,
                Pubkey.create(pendingChannel.getRemoteNodePub()),
                TransactionHash.create(forceClosedChannel.getClosingTxid()),
                getHtlcOutpoints(forceClosedChannel),
                getOpenInitiator(pendingChannel.getInitiator()),
                resolveIsPrivate(id)
        ));
    }

    private Set<ChannelPoint> getHtlcOutpoints(PendingChannelsResponse.ForceClosedChannel forceClosedChannel) {
        return forceClosedChannel.getPendingHtlcsList().stream()
                .map(PendingHTLC::getOutpoint)
                .map(ChannelPoint::create)
                .collect(toSet());
    }

    private PendingOpenChannel toPendingOpenChannel(PendingChannel pendingChannel, Pubkey ownPubkey) {
        return new PendingOpenChannel(
                ChannelPoint.create(pendingChannel.getChannelPoint()),
                Coins.ofSatoshis(pendingChannel.getCapacity()),
                ownPubkey,
                Pubkey.create(pendingChannel.getRemoteNodePub()),
                getOpenInitiator(pendingChannel.getInitiator()),
                pendingChannel.getPrivate()
        );
    }

    private LocalOpenChannel toLocalOpenChannel(Channel lndChannel, Pubkey ownPubkey) {
        BalanceInformation balanceInformation = new BalanceInformation(
                Coins.ofSatoshis(lndChannel.getLocalBalance()),
                Coins.ofSatoshis(lndChannel.getLocalConstraints().getChanReserveSat()),
                Coins.ofSatoshis(lndChannel.getRemoteBalance()),
                Coins.ofSatoshis(lndChannel.getRemoteConstraints().getChanReserveSat())
        );
        return new LocalOpenChannel(
                new ChannelCoreInformation(
                        ChannelId.fromShortChannelId(lndChannel.getChanId()),
                        ChannelPoint.create(lndChannel.getChannelPoint()),
                        Coins.ofSatoshis(lndChannel.getCapacity())
                ), ownPubkey,
                Pubkey.create(lndChannel.getRemotePubkey()),
                balanceInformation,
                getOpenInitiator(lndChannel),
                Coins.ofSatoshis(lndChannel.getTotalSatoshisSent()),
                Coins.ofSatoshis(lndChannel.getTotalSatoshisReceived()),
                lndChannel.getPrivate(),
                lndChannel.getActive(),
                lndChannel.getNumUpdates()
        );
    }

    private OpenInitiator getOpenInitiator(Channel lndChannel) {
        if (lndChannel.getInitiator()) {
            return OpenInitiator.LOCAL;
        }
        return OpenInitiator.REMOTE;
    }
}
