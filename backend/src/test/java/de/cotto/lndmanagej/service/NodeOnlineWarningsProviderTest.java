package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.warnings.NodeOnlineChangesWarning;
import de.cotto.lndmanagej.model.warnings.NodeOnlinePercentageWarning;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_CHANGES_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_PERCENTAGE_THRESHOLD;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.ONLINE_WARNING_IGNORE_NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeOnlineWarningsProviderTest {
    @InjectMocks
    private NodeOnlineWarningsProvider warningsProvider;

    @Mock
    private OnlinePeersService onlinePeersService;

    @Mock
    private ConfigurationService configurationService;

    private void mockOnlineAndNoConfig() {
        when(onlinePeersService.getOnlinePercentage(PUBKEY)).thenReturn(80);
        when(onlinePeersService.getChanges(PUBKEY)).thenReturn(50);
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
    }

    @Test
    void getNodeWarnings_online_below_threshold() {
        mockOnlineAndNoConfig();
        when(onlinePeersService.getOnlinePercentage(PUBKEY)).thenReturn(79);
        when(onlinePeersService.getDaysForOnlinePercentage()).thenReturn(456);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlinePercentageWarning(79, 456));
    }

    @Test
    void getNodeWarnings_online_below_configured_threshold() {
        mockOnlineAndNoConfig();
        when(configurationService.getIntegerValue(ONLINE_PERCENTAGE_THRESHOLD)).thenReturn(Optional.of(99));
        when(onlinePeersService.getOnlinePercentage(PUBKEY)).thenReturn(98);
        when(onlinePeersService.getDaysForOnlinePercentage()).thenReturn(456);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlinePercentageWarning(98, 456));
    }

    @Test
    void getNodeWarnings_online_changes_above_threshold() {
        mockOnlineAndNoConfig();
        when(onlinePeersService.getChanges(PUBKEY)).thenReturn(51);
        when(onlinePeersService.getDaysForChanges()).thenReturn(123);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlineChangesWarning(51, 123));
    }

    @Test
    void getNodeWarnings_online_changes_above_configured_threshold() {
        mockOnlineAndNoConfig();
        when(configurationService.getIntegerValue(ONLINE_CHANGES_THRESHOLD)).thenReturn(Optional.of(30));
        when(onlinePeersService.getChanges(PUBKEY)).thenReturn(40);
        when(onlinePeersService.getDaysForChanges()).thenReturn(123);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY))
                .containsExactly(new NodeOnlineChangesWarning(40, 123));
    }

    @Test
    void getNodeWarnings_ok() {
        mockOnlineAndNoConfig();
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void getNodeWarnings_ignoredViaConfig_noWarning() {
        when(configurationService.getPubkeys(ONLINE_WARNING_IGNORE_NODE)).thenReturn(Set.of(PUBKEY));
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }
}
