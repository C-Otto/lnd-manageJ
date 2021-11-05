package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Node;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GrpcChannels {
    private final GrpcService grpcService;
    private final GrpcNodeInfo grpcNodeInfo;
    private final Node ownNode;

    public GrpcChannels(
            GrpcService grpcService,
            GrpcGetInfo grpcGetInfo, GrpcNodeInfo grpcNodeInfo
    ) {
        this.grpcService = grpcService;
        this.grpcNodeInfo = grpcNodeInfo;
        ownNode = grpcGetInfo.getNode();
    }

    public Set<Channel> getChannels() {
        return grpcService.getChannels().stream().map(this::toChannel).collect(Collectors.toSet());
    }

    private Channel toChannel(lnrpc.Channel lndChannel) {
        return Channel.builder()
                .withChannelId(ChannelId.fromShortChannelId(lndChannel.getChanId()))
                .withCapacity(Coins.ofSatoshis(lndChannel.getCapacity()))
                .withNode1(ownNode)
                .withNode2(grpcNodeInfo.getNode(lndChannel.getRemotePubkey()))
                .build();
    }

}
