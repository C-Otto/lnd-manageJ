package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import lnrpc.Peer;
import org.springframework.stereotype.Component;

@Component
public class GrpcNodeInfo {
    private final GrpcService grpcService;

    public GrpcNodeInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Node getNode(Pubkey pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey).orElse(null);
        if (nodeInfo == null) {
            return Node.forPubkey(pubkey);
        }
        LightningNode node = nodeInfo.getNode();
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(node.getAlias())
                .withLastUpdate(node.getLastUpdate())
                .build();
    }

    public Node getNodeWithOnlineStatus(Pubkey pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey).orElse(null);
        if (nodeInfo == null) {
            return Node.forPubkey(pubkey);
        }
        LightningNode node = nodeInfo.getNode();
        boolean isPeer = grpcService.listPeers().stream()
                .map(Peer::getPubKey)
                .map(Pubkey::create)
                .anyMatch(pubkey::equals);
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(node.getAlias())
                .withLastUpdate(node.getLastUpdate())
                .withOnlineStatus(isPeer)
                .build();
    }
}
