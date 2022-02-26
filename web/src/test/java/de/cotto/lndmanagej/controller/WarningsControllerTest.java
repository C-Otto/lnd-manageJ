package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.controller.dto.ChannelWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodeWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto;
import de.cotto.lndmanagej.controller.dto.WarningsDto;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
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

    @Mock
    private ChannelWarningsService channelWarningsService;

    @Test
    void getWarningsForNode() {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        assertThat(warningsController.getWarningsForNode(PUBKEY))
                .isEqualTo(WarningsDto.createFromModel(NODE_WARNINGS));
    }

    @Test
    void getWarningsForChannel() {
        when(channelWarningsService.getChannelWarnings(CHANNEL_ID)).thenReturn(CHANNEL_WARNINGS);
        assertThat(warningsController.getWarningsForChannel(CHANNEL_ID))
                .isEqualTo(WarningsDto.createFromModel(CHANNEL_WARNINGS));
    }

    @Test
    void getWarnings_empty() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of());
        assertThat(warningsController.getWarnings()).isEqualTo(NodesAndChannelsWithWarningsDto.NONE);
    }

    @Test
    void getWarnings() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE, NODE_WARNINGS, NODE_2, NODE_WARNINGS_2));
        when(channelWarningsService.getChannelWarnings()).thenReturn(Map.of(LOCAL_OPEN_CHANNEL, CHANNEL_WARNINGS));
        Set<String> descriptions1 = WarningsDto.createFromModel(NODE_WARNINGS).warnings();
        Set<String> descriptions2 = WarningsDto.createFromModel(NODE_WARNINGS_2).warnings();
        Set<String> descriptions3 = WarningsDto.createFromModel(CHANNEL_WARNINGS).warnings();
        assertThat(warningsController.getWarnings()).isEqualTo(new NodesAndChannelsWithWarningsDto(List.of(
                new NodeWithWarningsDto(descriptions1, ALIAS, PUBKEY),
                new NodeWithWarningsDto(descriptions2, ALIAS_2, PUBKEY_2)
        ),
                List.of(new ChannelWithWarningsDto(descriptions3, CHANNEL_ID))
        ));
    }

    @Test
    void getWarnings_sorted_by_pubkey() {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of(NODE_2, NODE_WARNINGS_2, NODE, NODE_WARNINGS));
        assertThat(warningsController.getWarnings().nodesWithWarnings()).map(NodeWithWarningsDto::pubkey)
                .containsExactly(PUBKEY, PUBKEY_2);
    }
}