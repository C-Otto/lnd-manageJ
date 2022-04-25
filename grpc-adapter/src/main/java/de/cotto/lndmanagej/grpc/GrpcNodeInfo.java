package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.configuration.ConfigurationService;
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
    private final ConfigurationService configurationService;

    public GrpcNodeInfo(GrpcService grpcService, ConfigurationService configurationService) {
        this.grpcService = grpcService;
        this.configurationService = configurationService;
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
        return configurationService.getHardcodedAlias(pubkey).orElse(node.getAlias());
    }

    private Node createNode(Pubkey pubkey) {
        Optional<String> hardcodedAlias = configurationService.getHardcodedAlias(pubkey);
        if (hardcodedAlias.isEmpty()) {
            return Node.forPubkey(pubkey);
        }
        return Node.builder().withPubkey(pubkey).withAlias(hardcodedAlias.get()).build();
    }
}
