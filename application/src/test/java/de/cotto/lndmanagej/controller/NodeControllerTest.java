package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeControllerTest {
    @InjectMocks
    private NodeController nodeController;

    @Mock
    private NodeService nodeService;

    @Mock
    private Metrics metrics;

    @Test
    void getAlias() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);

        assertThat(nodeController.getAlias(PUBKEY_2)).isEqualTo(ALIAS_2);
        verify(metrics).mark(argThat(name -> name.endsWith(".getAlias")));
    }

    @Test
    void getNodeDetails() {
        NodeDetailsDto expectedDetails = new NodeDetailsDto(PUBKEY_2, ALIAS_2, true);
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, true));

        assertThat(nodeController.getDetails(PUBKEY_2)).isEqualTo(expectedDetails);
        verify(metrics).mark(argThat(name -> name.endsWith(".getDetails")));
    }
}