package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.NodeWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesWithWarningsDto;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS_2;
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

    @Test
    void getWarnings_empty() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        assertThat(warningsController.getWarnings()).isEqualTo(NodesWithWarningsDto.NONE);
    }

    @Test
    void getWarnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS, NODE_2, NODE_WARNINGS_2));
        Set<String> descriptions = NodeWarningsDto.createFromModel(NODE_WARNINGS).nodeWarnings();
        Set<String> descriptions2 = NodeWarningsDto.createFromModel(NODE_WARNINGS_2).nodeWarnings();
        assertThat(warningsController.getWarnings()).isEqualTo(new NodesWithWarningsDto(List.of(
                new NodeWithWarningsDto(descriptions, ALIAS, PUBKEY),
                new NodeWithWarningsDto(descriptions2, ALIAS_2, PUBKEY_2)
        )));
    }
}