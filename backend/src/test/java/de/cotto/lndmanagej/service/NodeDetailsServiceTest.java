package de.cotto.lndmanagej.service;

import de.cotto.lndmanagej.model.BalanceInformation;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.model.FlowReport;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.model.OnChainCosts;
import de.cotto.lndmanagej.model.RebalanceReport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_2;
import static de.cotto.lndmanagej.model.FeeReportFixtures.FEE_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_4;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS_EMPTY;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT;
import static de.cotto.lndmanagej.model.OnlineReportFixtures.ONLINE_REPORT_OFFLINE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RebalanceReportFixtures.REBALANCE_REPORT;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NodeDetailsServiceTest {
    @InjectMocks
    private NodeDetailsService nodeDetailsService;

    @Mock
    private NodeService nodeService;

    @Mock
    private OnChainCostService onChainCostService;

    @Mock
    private BalanceService balanceService;

    @Mock
    private ChannelService channelService;

    @Mock
    private FeeService feeService;

    @Mock
    private FlowService flowService;

    @Mock
    private RebalanceService rebalanceService;

    @Mock
    private OnlinePeersService onlinePeersService;

    @Mock
    private NodeWarningsService nodeWarningsService;

    @Test
    void getDetails_no_channel() {
        when(nodeService.getNode(PUBKEY)).thenReturn(NODE);
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BalanceInformation.EMPTY);
        when(onChainCostService.getOnChainCostsForPeer(PUBKEY)).thenReturn(OnChainCosts.NONE);
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FeeReport.EMPTY);
        when(flowService.getFlowReportForPeer(PUBKEY)).thenReturn(FlowReport.EMPTY);
        when(rebalanceService.getReportForPeer(PUBKEY)).thenReturn(RebalanceReport.EMPTY);
        when(onlinePeersService.getOnlineReport(NODE_PEER)).thenReturn(ONLINE_REPORT_OFFLINE);
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NodeWarnings.NONE);
        assertThat(nodeDetailsService.getDetails(PUBKEY)).isEqualTo(NODE_DETAILS_EMPTY);
    }

    @Test
    void getDetails() {
        when(nodeService.getNode(PUBKEY)).thenReturn(NODE_PEER);
        when(onChainCostService.getOnChainCostsForPeer(PUBKEY)).thenReturn(ON_CHAIN_COSTS);
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BALANCE_INFORMATION_2);
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL));
        when(channelService.getClosedChannelsWith(PUBKEY)).thenReturn(Set.of(CLOSED_CHANNEL_2));
        when(channelService.getWaitingCloseChannelsWith(PUBKEY)).thenReturn(Set.of(WAITING_CLOSE_CHANNEL_TO_NODE_3));
        when(channelService.getForceClosingChannelsWith(PUBKEY)).thenReturn(Set.of(FORCE_CLOSING_CHANNEL_4));
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FEE_REPORT);
        when(flowService.getFlowReportForPeer(PUBKEY)).thenReturn(FLOW_REPORT);
        when(rebalanceService.getReportForPeer(PUBKEY)).thenReturn(REBALANCE_REPORT);
        when(onlinePeersService.getOnlineReport(NODE_PEER)).thenReturn(ONLINE_REPORT);
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        assertThat(nodeDetailsService.getDetails(PUBKEY)).isEqualTo(NODE_DETAILS);
    }
}