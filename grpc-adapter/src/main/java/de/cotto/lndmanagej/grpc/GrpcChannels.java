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
    private final GrpcNodeInfo grpcNodeInfo;

    public GrpcChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo,
            GrpcNodeInfo grpcNodeInfo
    ) {
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
        this.grpcNodeInfo = grpcNodeInfo;
    }

    public Set<Channel> getChannels() {
        return grpcService.getChannels().stream()
                .map(this::toChannel)
                .collect(toSet());
    }

    private Channel toChannel(lnrpc.Channel lndChannel) {
        return Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
                .withNode1(grpcGetInfo.getNode())
                .withNode2(grpcNodeInfo.getNode(Pubkey.create(lndChannel.getRemotePubkey())))
                .build();
    }

}
