package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.NodeWarningsDto;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningsControllerTest {
    @InjectMocks
    private WarningsController warningsController;

    @Mock
    private NodeWarningsService nodeWarningsService;

    @Test
    void getWarningsForNode() {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        assertThat(warningsController.getWarningsForNode(PUBKEY))
                .isEqualTo(NodeWarningsDto.createFromModel(NODE_WARNINGS));
    }
}