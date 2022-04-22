package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.hardcoded.HardcodedService;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import lnrpc.Peer;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
            return createNode(pubkey);
        }
        LightningNode node = nodeInfo.getNode();
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(getAlias(pubkey, node))
                .withLastUpdate(node.getLastUpdate())
                .build();
    }

    public Node getNodeWithOnlineStatus(Pubkey pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey).orElse(null);
        if (nodeInfo == null) {
            return createNode(pubkey);
        }
        LightningNode node = nodeInfo.getNode();
        boolean isPeer = grpcService.listPeers().stream()
                .map(Peer::getPubKey)
                .map(Pubkey::create)
                .anyMatch(pubkey::equals);
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(getAlias(pubkey, node))
                .withLastUpdate(node.getLastUpdate())
                .withOnlineStatus(isPeer)
                .build();
    }

    private String getAlias(Pubkey pubkey, LightningNode node) {
        return hardcodedService.getAlias(pubkey).orElse(node.getAlias());
    }

    private Node createNode(Pubkey pubkey) {
        Optional<String> hardcodedAlias = hardcodedService.getAlias(pubkey);
        if (hardcodedAlias.isEmpty()) {
            return Node.forPubkey(pubkey);
        }
        return Node.builder().withPubkey(pubkey).withAlias(hardcodedAlias.get()).build();
    }
}
