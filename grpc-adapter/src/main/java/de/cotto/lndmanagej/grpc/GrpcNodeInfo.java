package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Node;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import org.springframework.stereotype.Component;

@Component
public class GrpcNodeInfo {
    private final GrpcService grpcService;

    public GrpcNodeInfo(GrpcService grpcService) {
        this.grpcService = grpcService;
    }

    public Node getNode(String pubkey) {
        NodeInfo nodeInfo = grpcService.getNodeInfo(pubkey);
        LightningNode node = nodeInfo.getNode();
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(node.getAlias())
                .withLastUpdate(node.getLastUpdate())
                .build();
    }
}
