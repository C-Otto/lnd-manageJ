package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
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
        when(grpcNodeInfo.getAlias(PUBKEY)).thenReturn(ALIAS);
        assertThat(nodeService.getAlias(PUBKEY)).isEqualTo(ALIAS);
    }

    @Test
    void getNode() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE);
        assertThat(nodeService.getNode(PUBKEY)).isEqualTo(NODE);
    }

    @Test
    void getNode_updates_alias_cache() {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE_WITHOUT_ALIAS).thenReturn(NODE).thenThrow();
        assertThat(nodeService.getNode(PUBKEY)).isEqualTo(NODE_WITHOUT_ALIAS);
        String alias = nodeService.getAlias(PUBKEY);
        assertThat(alias).isEqualTo(NODE_WITHOUT_ALIAS.alias());
        verify(grpcNodeInfo, times(1)).getNode(PUBKEY);
    }
}