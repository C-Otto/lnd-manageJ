package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.configuration.ConfigurationService;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.warnings.NodeNoFlowWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_MINIMUM_DAYS_FOR_WARNING;
import static de.cotto.lndmanagej.configuration.WarningsConfigurationSettings.NODE_FLOW_WARNING_IGNORE_NODE;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.transactions.model.TransactionFixtures.BLOCK_HEIGHT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeFlowWarningsProviderTest {
    private static final int EXPECTED_BLOCKS_PER_DAY = 144;

    @InjectMocks
    private NodeFlowWarningsProvider warningsProvider;

    @Mock
    private FlowService flowService;

    @Mock
    private OwnNodeService ownNodeService;

    @Mock
    private ChannelService channelService;

    @Mock
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        lenient().when(flowService.getFlowReportForPeer(eq(PUBKEY), any())).thenReturn(FlowReport.EMPTY);
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(800_000);
        lenient().when(configurationService.getIntegerValue(any())).thenReturn(Optional.empty());
    }

    @Test
    void getNodeWarnings_no_flow_old_channel() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getOpenHeight(LOCAL_OPEN_CHANNEL)).thenReturn(BLOCK_HEIGHT);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).containsExactly(new NodeNoFlowWarning(90));
    }

    @Test
    void getNodeWarnings_no_flow_very_recent_channel() {
        mockOpenChannelWithAgeInBlocks(50);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void getNodeWarnings_no_flow_young_channel() {
        mockOpenChannelWithAgeInBlocks(45 * EXPECTED_BLOCKS_PER_DAY);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).containsExactly(new NodeNoFlowWarning(45));
    }

    @Test
    void getNodeWarnings_no_flow_no_open_channel() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of());
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void getNodeWarnings_no_flow_then_some_flow() {
        mockOpenChannelWithAgeInBlocks(100 * EXPECTED_BLOCKS_PER_DAY);
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(30))).thenReturn(FlowReport.EMPTY);
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(31))).thenReturn(FlowReport.EMPTY);
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(32))).thenReturn(FLOW_REPORT);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).containsExactly(new NodeNoFlowWarning(31));
    }

    @Test
    void getNodeWarnings_ok() {
        mockOpenChannelWithAgeInBlocks(100 * EXPECTED_BLOCKS_PER_DAY);
        when(flowService.getFlowReportForPeer(any(), any())).thenReturn(FLOW_REPORT);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
    }

    @Test
    void uses_minimum_days_for_warning_from_configuration() {
        when(configurationService.getIntegerValue(NODE_FLOW_MINIMUM_DAYS_FOR_WARNING)).thenReturn(Optional.of(2));
        mockOpenChannelWithAgeInBlocks(3 * EXPECTED_BLOCKS_PER_DAY);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).containsExactly(new NodeNoFlowWarning(3));
    }

    @Test
    void uses_maximum_days_to_consider_from_configuration() {
        when(configurationService.getIntegerValue(NODE_FLOW_MAXIMUM_DAYS_TO_CONSIDER))
                .thenReturn(Optional.of(120));
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL_2));
        when(channelService.getOpenHeight(LOCAL_OPEN_CHANNEL_2)).thenReturn(BLOCK_HEIGHT);
        assertThat(warningsProvider.getNodeWarnings(PUBKEY)).containsExactly(new NodeNoFlowWarning(120));
    }

    @Nested
    class IgnoredWarnings {
        @BeforeEach
        void setUp() {
            mockOpenChannelWithAgeInBlocks(45 * EXPECTED_BLOCKS_PER_DAY);
        }

        @Test
        void no_warning_for_ignored_node() {
            when(configurationService.getPubkeys(NODE_FLOW_WARNING_IGNORE_NODE)).thenReturn(Set.of(PUBKEY_3, PUBKEY));
            assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isEmpty();
        }

        @Test
        void warning_if_other_node_is_ignored() {
            when(configurationService.getPubkeys(NODE_FLOW_WARNING_IGNORE_NODE)).thenReturn(Set.of(PUBKEY_3, PUBKEY_2));
            assertThat(warningsProvider.getNodeWarnings(PUBKEY)).isNotEmpty();
        }
    }

    private void mockOpenChannelWithAgeInBlocks(int channelAgeInBlocks) {
        lenient().when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        lenient().when(channelService.getOpenHeight(LOCAL_OPEN_CHANNEL)).thenReturn(BLOCK_HEIGHT);
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(BLOCK_HEIGHT + channelAgeInBlocks);
    }
}
