package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.model.NodeFixtures;
import de.cotto.lndmanagej.model.Pubkey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_WITHOUT_ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeServiceTest {
    @InjectMocks
    private NodeService nodeService;

    @Mock
    private GrpcNodeInfo grpcNodeInfo;

    @Test
    void getAlias() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE);
        assertThat(nodeService.getAlias(PUBKEY)).isEqualTo(ALIAS);
    }

    @Test
    void getAlias_hardcoded() {
        Pubkey blueWallet = Pubkey.create("037cc5f9f1da20ac0d60e83989729a204a33cc2d8e80438969fadf35c1c5f1233b");
        Node node = Node.builder()
                .withPubkey(blueWallet)
                .withAlias(ALIAS)
                .withLastUpdate(NodeFixtures.LAST_UPDATE)
                .build();
        when(grpcNodeInfo.getNode(blueWallet)).thenReturn(node);
        assertThat(nodeService.getAlias(blueWallet)).isEqualTo("BlueWallet");
    }

    @Test
    void getNode() {
        when(grpcNodeInfo.getNodeWithOnlineStatus(PUBKEY)).thenReturn(NODE);
        assertThat(nodeService.getNode(PUBKEY)).isEqualTo(NODE);
    }

    @Test
    void getNodeWithOnl_updates_alias_cache() {
        when(grpcNodeInfo.getNodeWithOnlineStatus(PUBKEY)).thenReturn(NODE_WITHOUT_ALIAS).thenReturn(NODE).thenThrow();
        assertThat(nodeService.getNode(PUBKEY)).isEqualTo(NODE_WITHOUT_ALIAS);
        String alias = nodeService.getAlias(PUBKEY);
        assertThat(alias).isEqualTo(NODE_WITHOUT_ALIAS.alias());
        verify(grpcNodeInfo, times(1)).getNodeWithOnlineStatus(PUBKEY);
    }
}