package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.LocalChannel;
import de.cotto.lndmanagej.model.Pubkey;
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

    public Set<LocalChannel> getChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        return grpcService.getChannels().stream()
                .map(lndChannel -> toChannel(lndChannel, ownPubkey))
                .collect(toSet());
    }

    public Optional<LocalChannel> getChannel(ChannelId channelId) {
        Pubkey ownPubkey = grpcGetInfo.getPubkey();
        long expectedChannelId = channelId.shortChannelId();
        return grpcService.getChannels().stream()
                .filter(c -> c.getChanId() == expectedChannelId)
                .map(lndChannel -> toChannel(lndChannel, ownPubkey))
                .findFirst();
    }

    private LocalChannel toChannel(lnrpc.Channel lndChannel, Pubkey ownPubkey) {
        Channel channel = Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
                .withNode1(ownPubkey)
                .withNode2(Pubkey.create(lndChannel.getRemotePubkey()))
                .build();
        Coins localBalance = Coins.ofSatoshis(lndChannel.getLocalBalance());
        Coins localReserve = Coins.ofSatoshis(lndChannel.getLocalConstraints().getChanReserveSat());
        return new LocalChannel(channel, ownPubkey, localBalance, localReserve);
    }

}
