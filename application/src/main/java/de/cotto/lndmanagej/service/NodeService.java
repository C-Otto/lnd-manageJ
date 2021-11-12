package de.cotto.lndmanagej.service;

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
    private final ChannelService channelService;

    public NodeService(GrpcNodeInfo grpcNodeInfo, ChannelService channelService) {
        this.grpcNodeInfo = grpcNodeInfo;
        this.channelService = channelService;
    }

    public List<ChannelId> getOpenChannelIds(Pubkey pubkey) {
        Node node = getNode(pubkey);
        return channelService.getOpenChannelsWith(node).stream()
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
