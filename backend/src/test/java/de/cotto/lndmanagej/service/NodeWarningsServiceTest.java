package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.warnings.NodeWarningsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.warnings.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeWarningsServiceTest {
    private NodeWarningsService nodeWarningsService;

    @Mock
    private NodeWarningsProvider provider1;

    @Mock
    private NodeWarningsProvider provider2;

    @Mock
    private NodeService nodeService;

    @Mock
    private ChannelService channelService;

    @BeforeEach
    void setUp() {
        nodeWarningsService = new NodeWarningsService(Set.of(provider1, provider2), channelService, nodeService);
        lenient().when(provider1.getNodeWarnings(PUBKEY_2)).thenReturn(Stream.of());
        lenient().when(provider1.getNodeWarnings(PUBKEY_3)).thenReturn(Stream.of());
        lenient().when(provider2.getNodeWarnings(PUBKEY_2)).thenReturn(Stream.of());
        lenient().when(provider2.getNodeWarnings(PUBKEY_3)).thenReturn(Stream.of());
        lenient().when(nodeService.getNode(PUBKEY_2)).thenReturn(NODE_2);
        lenient().when(nodeService.getNode(PUBKEY_3)).thenReturn(NODE_3);
    }

    @Test
    void getNodeWarnings_for_one_node_no_warning() {
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY_2)).isEqualTo(NodeWarnings.NONE);
    }

    @Test
    void getNodeWarnings_for_one_node() {
        when(provider1.getNodeWarnings(PUBKEY_2))
                .thenReturn(Stream.of(NODE_ONLINE_PERCENTAGE_WARNING, NODE_ONLINE_CHANGES_WARNING));
        when(provider2.getNodeWarnings(PUBKEY_2))
                .thenReturn(Stream.of(NODE_NO_FLOW_WARNING));
        NodeWarnings expected = new NodeWarnings(
                NODE_NO_FLOW_WARNING,
                NODE_ONLINE_PERCENTAGE_WARNING,
                NODE_ONLINE_CHANGES_WARNING
        );
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY_2)).isEqualTo(expected);
    }

    @Test
    void getNodeWarnings_no_channel() {
        when(channelService.getOpenChannels()).thenReturn(Set.of());
        assertThat(nodeWarningsService.getNodeWarnings()).isEmpty();
    }

    @Test
    void getNodeWarnings_no_warnings() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        assertThat(nodeWarningsService.getNodeWarnings()).isEmpty();
    }

    @Test
    void getNodeWarnings() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_TO_NODE_3));
        when(provider1.getNodeWarnings(PUBKEY_2)).thenReturn(Stream.of(NODE_ONLINE_PERCENTAGE_WARNING));
        when(provider2.getNodeWarnings(PUBKEY_3)).thenReturn(Stream.of(NODE_ONLINE_CHANGES_WARNING));
        assertThat(nodeWarningsService.getNodeWarnings()).containsExactlyInAnyOrderEntriesOf(Map.of(
                NODE_2, new NodeWarnings(NODE_ONLINE_PERCENTAGE_WARNING),
                NODE_3, new NodeWarnings(NODE_ONLINE_CHANGES_WARNING)
        ));
    }

    @Test
    void getNodeWarnings_duplicate_nodes() {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(provider1.getNodeWarnings(PUBKEY_2)).thenReturn(Stream.of(NODE_ONLINE_CHANGES_WARNING));
        when(provider2.getNodeWarnings(PUBKEY_2)).thenReturn(Stream.of(NODE_ONLINE_PERCENTAGE_WARNING));
        assertThat(nodeWarningsService.getNodeWarnings()).containsEntry(
                NODE_2, new NodeWarnings(NODE_ONLINE_CHANGES_WARNING, NODE_ONLINE_PERCENTAGE_WARNING)
        );
    }
}
