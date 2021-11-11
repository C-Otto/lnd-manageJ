package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.ChannelId;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NodeService {
    private final GrpcNodeInfo grpcNodeInfo;
    private final GrpcChannels grpcChannels;

    public NodeService(GrpcNodeInfo grpcNodeInfo, GrpcChannels grpcChannels) {
        this.grpcNodeInfo = grpcNodeInfo;
        this.grpcChannels = grpcChannels;
    }

    public List<ChannelId> getOpenChannelIds(Pubkey pubkey) {
        Node node = getNode(pubkey);
        return grpcChannels.getChannels().stream()
                .filter(c -> c.getNodes().contains(node))
                .map(Channel::getId)
                .sorted()
                .collect(Collectors.toList());
    }

    public String getAlias(Pubkey pubkey) {
        return getNode(pubkey).alias();
    }

    private Node getNode(Pubkey pubkey) {
        return grpcNodeInfo.getNode(pubkey);
    }
}
