package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.NodeOnlinePercentageWarning;
import de.cotto.lndmanagej.model.NodeWarnings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeWarningsServiceTest {
    @InjectMocks
    private NodeWarningsService nodeWarningsService;

    @Mock
    private OnlinePeersService onlinePeersService;

    @BeforeEach
    void setUp() {
        when(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).thenReturn(80);
        when(onlinePeersService.getChangesLastWeek(PUBKEY)).thenReturn(50);
    }

    @Test
    void getNodeWarnings_online_below_threshold() {
        when(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).thenReturn(79);
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY))
                .isEqualTo(new NodeWarnings(new NodeOnlinePercentageWarning(79)));
    }

    @Test
    void getNodeWarnings_online_changes_above_threshold() {
        when(onlinePeersService.getChangesLastWeek(PUBKEY)).thenReturn(51);
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY))
                .isEqualTo(new NodeWarnings(new NodeOnlineChangesWarning(51)));
    }

    @Test
    void getNodeWarnings_all_warnings() {
        when(onlinePeersService.getOnlinePercentageLastWeek(PUBKEY)).thenReturn(79);
        when(onlinePeersService.getChangesLastWeek(PUBKEY)).thenReturn(51);
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY))
                .isEqualTo(new NodeWarnings(new NodeOnlinePercentageWarning(79), new NodeOnlineChangesWarning(51)));
    }

    @Test
    void getNodeWarnings_ok() {
        assertThat(nodeWarningsService.getNodeWarnings(PUBKEY)).isEqualTo(NodeWarnings.NONE);
    }
}