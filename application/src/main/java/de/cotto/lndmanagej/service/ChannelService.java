package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcChannels;
import de.cotto.lndmanagej.model.Channel;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChannelService {
    private final GrpcChannels grpcChannels;

    public ChannelService(GrpcChannels grpcChannels) {
        this.grpcChannels = grpcChannels;
    }

    public Set<Channel> getOpenChannelsWith(Pubkey peer) {
        Node peerNode = Node.forPubkey(peer);
        return getOpenChannelsWith(peerNode);
    }

    public Set<Channel> getOpenChannelsWith(Node peer) {
        return grpcChannels.getChannels().stream()
                .filter(c -> c.getNodes().contains(peer))
                .collect(Collectors.toSet());
    }
}
