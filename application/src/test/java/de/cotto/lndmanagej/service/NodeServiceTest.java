package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
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
}