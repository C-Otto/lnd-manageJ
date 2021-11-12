package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
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
            return Node.builder().withPubkey(pubkey).build();
        }
        LightningNode node = nodeInfo.getNode();
        return Node.builder()
                .withPubkey(pubkey)
                .withAlias(node.getAlias())
                .withLastUpdate(node.getLastUpdate())
                .build();
    }
}
