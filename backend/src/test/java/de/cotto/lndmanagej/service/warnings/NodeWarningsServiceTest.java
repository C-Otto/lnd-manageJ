package de.cotto.lndmanagej.service.warnings;

import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.service.NodeWarningsProvider;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Stream;

import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_NO_FLOW_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_CHANGES_WARNING;
import static de.cotto.lndmanagej.model.NodeWarningFixtures.NODE_ONLINE_PERCENTAGE_WARNING;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeWarningsServiceTest {
    private NodeWarningsService nodeWarningsService;
    private NodeWarningsProvider provider1;
    private NodeWarningsProvider provider2;

    @BeforeEach
    void setUp() {
        provider1 = mock(NodeWarningsProvider.class);
        provider2 = mock(NodeWarningsProvider.class);
        nodeWarningsService = new NodeWarningsService(Set.of(provider1, provider2));
    }

    @Test
    void getNodeWarnings_no_warning() {
        when(provider1.getNodeWarnings(PUBKEY)).thenReturn(Stream.of());
        when(provider2.getNodeWarnings(PUBKEY)).thenReturn(Stream.of());
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY)).isEqualTo(NodeWarnings.NONE);
    }

    @Test
    void getNodeWarnings() {
        when(provider1.getNodeWarnings(PUBKEY))
                .thenReturn(Stream.of(NODE_ONLINE_PERCENTAGE_WARNING, NODE_ONLINE_CHANGES_WARNING));
        when(provider2.getNodeWarnings(PUBKEY))
                .thenReturn(Stream.of(NODE_NO_FLOW_WARNING));
        NodeWarnings expected = new NodeWarnings(
                NODE_NO_FLOW_WARNING,
                NODE_ONLINE_PERCENTAGE_WARNING,
                NODE_ONLINE_CHANGES_WARNING
        );
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY)).isEqualTo(expected);
    }
}