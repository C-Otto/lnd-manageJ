package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

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

    public Set<Channel> getChannels() {
        Pubkey ownPubkey = grpcGetInfo.getPubkey().orElseThrow();
        return grpcService.getChannels().stream()
                .map(lndChannel -> toChannel(lndChannel, ownPubkey))
                .collect(toSet());
    }

    private Channel toChannel(lnrpc.Channel lndChannel, Pubkey ownPubkey) {
        return Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
                .withNode1(ownPubkey)
                .withNode2(Pubkey.create(lndChannel.getRemotePubkey()))
                .build();
    }

}
