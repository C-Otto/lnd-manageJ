package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.ClosedChannel;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalOpenChannel;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.ChannelCloseSummary;
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

    public Set<ClosedChannel> getClosedChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getClosedChannels().stream()
                .map(channelCloseSummary -> toClosedChannel(channelCloseSummary, ownPubkey))
                .collect(toSet());
    }

    public Optional<LocalOpenChannel> getChannel(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        long expectedChannelId = channelId.shortChannelId();
        return grpcService.getChannels().stream()
                .filter(c -> c.getChanId() == expectedChannelId)
                .map(lndChannel -> toLocalOpenChannel(lndChannel, ownPubkey))
                .findFirst();
    }

    private LocalOpenChannel toLocalOpenChannel(lnrpc.Channel lndChannel, Pubkey ownPubkey) {
        Channel channel = Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
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

    private ClosedChannel toClosedChannel(ChannelCloseSummary channelCloseSummary, Pubkey ownPubkey) {
        Channel channel = Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(channelCloseSummary.getChanId()))
                .withCapacity(Coins.ofSatoshis(channelCloseSummary.getCapacity()))
                .withNode1(ownPubkey)
                .withNode2(Pubkey.create(channelCloseSummary.getRemotePubkey()))
                .build();
        return new ClosedChannel(channel, ownPubkey);
    }
}
