package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;
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
        when(onlinePeersService.getOnlinePercentage(PUBKEY)).thenReturn(80);
        when(onlinePeersService.getChanges(PUBKEY)).thenReturn(50);
    }

    @Test
    void getNodeWarnings_online_below_threshold() {
        when(onlinePeersService.getOnlinePercentage(PUBKEY)).thenReturn(79);
        when(onlinePeersService.getDaysForOnlinePercentage()).thenReturn(456);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlinePercentageWarning(79, 456));
    }

    @Test
    void getNodeWarnings_online_changes_above_threshold() {
        when(onlinePeersService.getChanges(PUBKEY)).thenReturn(51);
        when(onlinePeersService.getDaysForChanges()).thenReturn(123);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlineChangesWarning(51, 123));
    }

    @Test
    void getNodeWarnings_ok() {
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }
}