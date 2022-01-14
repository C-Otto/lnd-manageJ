package de.cotto.lndmanagej.service.warnings;

import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;
import de.cotto.lndmanagej.service.OnlinePeersService;
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
class NodeOnlineWarningsProviderTest {
    @InjectMocks
    private NodeOnlineWarningsProvider warningsProvider;

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
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlinePercentageWarning(79));
    }

    @Test
    void getNodeWarnings_online_changes_above_threshold() {
        when(onlinePeersService.getChangesLastWeek(PUBKEY)).thenReturn(51);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlineChangesWarning(51));
    }

    @Test
    void getNodeWarnings_ok() {
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }
}