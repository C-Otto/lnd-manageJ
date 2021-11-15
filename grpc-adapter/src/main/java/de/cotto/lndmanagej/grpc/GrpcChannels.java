package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ChannelPoint;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import de.cotto.lndmanagej.model.UnresolvedClosedChannel;
import lnrpc.ChannelCloseSummary;
import lnrpc.ChannelCloseSummary.ClosureType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Component
public class GrpcChannels {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;

    public GrpcChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo
    ) {
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
    }

    public Set<LocalOpenChannel> getChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getChannels().stream()
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .collect(toSet());
    }

    public Set<UnresolvedClosedChannel> getUnresolvedClosedChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getClosedChannels().stream()
                .filter(this::shouldConsider)
                .map(channelCloseSummary -> toUnresolvedClosedChannel(channelCloseSummary, ownPubkey))
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
        Channel channel = Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
                .withChannelPoint(ChannelPoint.create(lndChannel.getChannelPoint()))
                .withNode1(ownPubkey)
                .withNode2(Pubkey.create(lndChannel.getRemotePubkey()))
                .build();
        BalanceInformation balanceInformation = new BalanceInformation(
                Coins.ofSatoshis(lndChannel.getLocalBalance()),
                Coins.ofSatoshis(lndChannel.getLocalConstraints().getChanReserveSat()),
                Coins.ofSatoshis(lndChannel.getRemoteBalance()),
                Coins.ofSatoshis(lndChannel.getRemoteConstraints().getChanReserveSat())
        );
        return new LocalOpenChannel(channel, ownPubkey, balanceInformation);
    }

    private UnresolvedClosedChannel toUnresolvedClosedChannel(
            ChannelCloseSummary channelCloseSummary,
            Pubkey ownPubkey
    ) {
        Channel channel = Channel.builder()
                .withChannelId(getChannelId(channelCloseSummary))
                .withChannelPoint(ChannelPoint.create(channelCloseSummary.getChannelPoint()))
                .withCapacity(Coins.ofSatoshis(channelCloseSummary.getCapacity()))
                .withNode1(ownPubkey)
                .withNode2(Pubkey.create(channelCloseSummary.getRemotePubkey()))
                .build();
        return new UnresolvedClosedChannel(channel, ownPubkey);
    }

    private ChannelId getChannelId(ChannelCloseSummary channelCloseSummary) {
        long chanId = channelCloseSummary.getChanId();
        if (chanId == 0) {
            return ChannelId.UNRESOLVED;
        }
        return ChannelId.fromShortChannelId(chanId);
    }

    private boolean shouldConsider(ChannelCloseSummary channelCloseSummary) {
        ClosureType closeType = channelCloseSummary.getCloseType();
        return closeType != ClosureType.ABANDONED && closeType != ClosureType.FUNDING_CANCELED;
    }
}
