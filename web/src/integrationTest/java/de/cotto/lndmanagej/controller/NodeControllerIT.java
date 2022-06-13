package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeDetailsService;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_4;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeDetailsFixtures.NODE_DETAILS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = NodeController.class)
class NodeControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @MockBean
    private ChannelService channelService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private NodeDetailsService nodeDetailsService;

    @Test
    void getAlias() throws Exception {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS_2);
        mockMvc.perform(get(NODE_PREFIX + "/alias"))
                .andExpect(content().string(ALIAS_2));
    }

    @Test
    void getDetails() throws Exception {
        when(nodeDetailsService.getDetails(PUBKEY)).thenReturn(NODE_DETAILS);
        List<String> channelIds = List.of(CHANNEL_ID.toString());
        List<String> closedChannelIds = List.of(CHANNEL_ID_2.toString());
        List<String> waitingCloseChannelIds = List.of(CHANNEL_ID_3.toString());
        List<String> forceClosingChannelIds = List.of(CHANNEL_ID_4.toString());
        mockMvc.perform(get(NODE_PREFIX + "/details"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.alias", is(ALIAS)))
                .andExpect(jsonPath("$.channels", is(channelIds)))
                .andExpect(jsonPath("$.closedChannels", is(closedChannelIds)))
                .andExpect(jsonPath("$.waitingCloseChannels", is(waitingCloseChannelIds)))
                .andExpect(jsonPath("$.pendingForceClosingChannels", is(forceClosingChannelIds)))
                .andExpect(jsonPath("$.rebalanceReport.sourceCostsMilliSat", is("1000000")))
                .andExpect(jsonPath("$.rebalanceReport.targetCostsMilliSat", is("2000000")))
                .andExpect(jsonPath("$.rebalanceReport.sourceAmountMilliSat", is("665000")))
                .andExpect(jsonPath("$.rebalanceReport.targetAmountMilliSat", is("991000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsSourceAmountMilliSat", is("100000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsTargetAmountMilliSat", is("200000")))
                .andExpect(jsonPath("$.flowReport.forwardedSentMilliSat", is("1050000")))
                .andExpect(jsonPath("$.flowReport.forwardedReceivedMilliSat", is("9001000")))
                .andExpect(jsonPath("$.flowReport.forwardingFeesReceivedMilliSat", is("1")))
                .andExpect(jsonPath("$.flowReport.rebalanceSentMilliSat", is("50000")))
                .andExpect(jsonPath("$.flowReport.rebalanceFeesSentMilliSat", is("5")))
                .andExpect(jsonPath("$.flowReport.rebalanceReceivedMilliSat", is("51000")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportSentMilliSat", is("123")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportFeesSentMilliSat", is("1")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportReceivedMilliSat", is("456")))
                .andExpect(jsonPath("$.flowReport.totalSentMilliSat", is("1100129")))
                .andExpect(jsonPath("$.flowReport.totalReceivedMilliSat", is("9052457")))
                .andExpect(jsonPath("$.balance.localBalanceSat", is("2000")))
                .andExpect(jsonPath("$.balance.localReserveSat", is("200")))
                .andExpect(jsonPath("$.balance.localAvailableSat", is("1800")))
                .andExpect(jsonPath("$.balance.remoteBalanceSat", is("223")))
                .andExpect(jsonPath("$.balance.remoteReserveSat", is("20")))
                .andExpect(jsonPath("$.balance.remoteAvailableSat", is("203")))
                .andExpect(jsonPath("$.feeReport.earnedMilliSat", is("1234")))
                .andExpect(jsonPath("$.feeReport.sourcedMilliSat", is("567")))
                .andExpect(jsonPath("$.warnings", containsInAnyOrder(
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days",
                        "No flow in the past 16 days"
                )))
                .andExpect(jsonPath("$.onChainCosts.openCostsSat", is("1000")))
                .andExpect(jsonPath("$.onChainCosts.closeCostsSat", is("2000")))
                .andExpect(jsonPath("$.onChainCosts.sweepCostsSat", is("3000")))
                .andExpect(jsonPath("$.onlineReport.online", is(true)))
                .andExpect(jsonPath("$.onlineReport.onlinePercentage", is(77)))
                .andExpect(jsonPath("$.onlineReport.daysForOnlinePercentage", is(14)))
                .andExpect(jsonPath("$.onlineReport.changes", is(5)))
                .andExpect(jsonPath("$.onlineReport.daysForChanges", is(7)))
                .andExpect(jsonPath("$.onlineReport.since", is("2021-12-23T01:02:03Z")))
                .andExpect(jsonPath("$.rating", is(123)));
    }

    @Test
    void getOpenChannelIds() throws Exception {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/open-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getAllChannelIds() throws Exception {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/all-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getBalance() throws Exception {
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BALANCE_INFORMATION);
        mockMvc.perform(get(NODE_PREFIX + "/balance"))
                .andExpect(jsonPath("$.localBalanceSat", is("1000")))
                .andExpect(jsonPath("$.localReserveSat", is("100")))
                .andExpect(jsonPath("$.localAvailableSat", is("900")))
                .andExpect(jsonPath("$.remoteBalanceSat", is("123")))
                .andExpect(jsonPath("$.remoteReserveSat", is("10")))
                .andExpect(jsonPath("$.remoteAvailableSat", is("113")));
    }

    @Test
    void getFeeReport() throws Exception {
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FEE_REPORT);
        mockMvc.perform(get(NODE_PREFIX + "/fee-report"))
                .andExpect(jsonPath("$.earnedMilliSat", is("1234")))
                .andExpect(jsonPath("$.sourcedMilliSat", is("567")));
    }

    @Test
    void getFeeReport_last_days() throws Exception {
        when(feeService.getFeeReportForPeer(PUBKEY, Duration.ofDays(123))).thenReturn(FEE_REPORT);
        mockMvc.perform(get(NODE_PREFIX + "/fee-report/last-days/123"))
                .andExpect(jsonPath("$.earnedMilliSat", is("1234")))
                .andExpect(jsonPath("$.sourcedMilliSat", is("567")));
    }
}