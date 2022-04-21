package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.hardcoded.HardcodedService;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import lnrpc.Peer;
import org.springframework.stereotype.Component;

@Component
public class GrpcNodeInfo {
    private final GrpcService grpcService;
    private final HardcodedService hardcodedService;

    public GrpcNodeInfo(GrpcService grpcService, HardcodedService hardcodedService) {
        this.grpcService = grpcService;
        this.hardcodedService = hardcodedService;
    }

    public Node getNode(Pubkey pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey).orElse(null);
        if (nodeInfo == null) {
            return Node.forPubkey(pubkey);
        }
        LightningNode node = nodeInfo.getNode();
        String alias = hardcodedService.getAliasOrDefault(pubkey, node.getAlias());
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(alias)
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
        String alias = hardcodedService.getAliasOrDefault(pubkey, node.getAlias());
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(alias)
                .withLastUpdate(node.getLastUpdate())
                .withOnlineStatus(isPeer)
                .build();
    }
}
