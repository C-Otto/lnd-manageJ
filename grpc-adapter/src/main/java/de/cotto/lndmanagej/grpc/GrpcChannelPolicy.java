package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.ChannelId;
import lnrpc.RoutingPolicy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GrpcChannelPolicy {
    private final GrpcService grpcService;
    private final GrpcGetInfo grpcGetInfo;

    public GrpcChannelPolicy(GrpcService grpcService, GrpcGetInfo grpcGetInfo) {
        this.grpcService = grpcService;
        this.grpcGetInfo = grpcGetInfo;
    }

    public Optional<RoutingPolicy> getLocalPolicy(ChannelId channelId) {
        String ownPubkey = grpcGetInfo.getPubkey().toString();
        return grpcService.getChannelEdge(channelId).map(
                channelEdge -> {
                    if (ownPubkey.equals(channelEdge.getNode1Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (ownPubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode2Policy();
                    } else {
                        return null;
                    }
                }
        );
    }

    public Optional<RoutingPolicy> getRemotePolicy(ChannelId channelId) {
        String ownPubkey = grpcGetInfo.getPubkey().toString();
        return grpcService.getChannelEdge(channelId).map(
                channelEdge -> {
                    if (ownPubkey.equals(channelEdge.getNode2Pub())) {
                        return channelEdge.getNode1Policy();
                    } else if (ownPubkey.equals(channelEdge.getNode1Pub())) {
                        return channelEdge.getNode2Policy();
                    } else {
                        return null;
                    }
                }
        );
    }

}
