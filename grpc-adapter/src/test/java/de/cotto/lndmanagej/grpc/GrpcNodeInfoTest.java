package de.cotto.lndmanagej.grpc;

import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GrpcNodeInfoTest {
    @InjectMocks
    private GrpcNodeInfo grpcNodeInfo;

    @Mock
    private GrpcService grpcService;

    @BeforeEach
    void setUp() {
        NodeInfo.Builder node = NodeInfo.newBuilder().setNode(LightningNode.newBuilder()
                .setAlias(NODE.alias())
                .setLastUpdate(NODE.lastUpdate())
                .build());
        when(grpcService.getNodeInfo(NODE.pubkey())).thenReturn(node.build());
    }

    @Test
    void getNode() {
        assertThat(grpcNodeInfo.getNode(NODE.pubkey())).isEqualTo(NODE);
    }

    @Test
    void getNode_sets_alias() {
        assertThat(grpcNodeInfo.getNode(NODE.pubkey()).alias()).isEqualTo(NODE.alias());
    }

    @Test
    void getNode_sets_last_update() {
        assertThat(grpcNodeInfo.getNode(NODE.pubkey()).lastUpdate()).isEqualTo(NODE.lastUpdate());
    }
}