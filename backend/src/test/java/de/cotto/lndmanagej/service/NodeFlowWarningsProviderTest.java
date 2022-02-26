package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.warnings.NodeNoFlowWarning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
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

    @BeforeEach
    void setUp() {
        lenient().when(flowService.getFlowReportForPeer(eq(PUBKEY), any())).thenReturn(FlowReport.EMPTY);
        lenient().when(ownNodeService.getBlockHeight()).thenReturn(800_000);
    }

    @Test
    void getNodeWarnings_no_flow_old_channel() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getOpenHeight(LOCAL_OPEN_CHANNEL)).thenReturn(Optional.of(BLOCK_HEIGHT));
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

    private void mockOpenChannelWithAgeInBlocks(int channelAgeInBlocks) {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getOpenHeight(LOCAL_OPEN_CHANNEL)).thenReturn(Optional.of(BLOCK_HEIGHT));
        when(ownNodeService.getBlockHeight()).thenReturn(BLOCK_HEIGHT + channelAgeInBlocks);
    }
}