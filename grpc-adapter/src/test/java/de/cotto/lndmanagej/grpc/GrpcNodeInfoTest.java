package de.cotto.lndmanagej.grpc;

import de.cotto.lndmanagej.model.Pubkey;
import lnrpc.LightningNode;
import lnrpc.NodeInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_WITHOUT_ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        when(grpcService.getNodeInfo(NODE.pubkey())).thenReturn(Optional.of(node.build()));
    }

    @Test
    void getNode() {
        assertThat(grpcNodeInfo.getNode(NODE.pubkey())).isEqualTo(NODE);
        verify(grpcService, never()).listPeers();
    }

    @Test
    void getNode_not_found() {
        when(grpcService.getNodeInfo(NODE.pubkey())).thenReturn(Optional.empty());
        assertThat(grpcNodeInfo.getNode(NODE.pubkey())).isEqualTo(NODE_WITHOUT_ALIAS);
    }

    @Test
    void getNodeWithOnlineStatus() {
        when(grpcService.listPeers()).thenReturn(List.of(peer(PUBKEY_2)));
        assertThat(grpcNodeInfo.getNodeWithOnlineStatus(NODE.pubkey())).isEqualTo(NODE);
    }

    @Test
    void getNodeWithOnlineStatus_for_peer() {
        when(grpcService.listPeers()).thenReturn(List.of(peer(PUBKEY)));
        assertThat(grpcNodeInfo.getNodeWithOnlineStatus(NODE.pubkey())).isEqualTo(NODE_PEER);
    }

    @Test
    void getNodeWithOnlineStatus_sets_alias() {
        assertThat(grpcNodeInfo.getNodeWithOnlineStatus(NODE.pubkey()).alias()).isEqualTo(NODE.alias());
    }

    @Test
    void getNodeWithOnlineStatus_sets_last_update() {
        assertThat(grpcNodeInfo.getNodeWithOnlineStatus(NODE.pubkey()).lastUpdate()).isEqualTo(NODE.lastUpdate());
    }

    @Test
    void getNodeWithOnlineStatus_error() {
        when(grpcService.getNodeInfo(NODE.pubkey())).thenReturn(Optional.empty());
        assertThat(grpcNodeInfo.getNodeWithOnlineStatus(NODE.pubkey()).alias()).isEqualTo(NODE.pubkey().toString());
    }

    private lnrpc.Peer peer(Pubkey pubkey) {
        return lnrpc.Peer.newBuilder().setPubKey(pubkey.toString()).build();
    }
}