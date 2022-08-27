package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.FlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FlowController.class)
class FlowControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlowService flowService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void getFlowReport_for_channel() throws Exception {
        when(flowService.getFlowReportForChannel(CHANNEL_ID)).thenReturn(FLOW_REPORT_2);
        mockMvc.perform(get(CHANNEL_PREFIX + "/flow-report"))
                .andExpect(jsonPath("$.forwardedSentMilliSat", is("1000")))
                .andExpect(jsonPath("$.forwardedReceivedMilliSat", is("2000")))
                .andExpect(jsonPath("$.forwardingFeesReceivedMilliSat", is("10")))
                .andExpect(jsonPath("$.rebalanceSentMilliSat", is("60000")))
                .andExpect(jsonPath("$.rebalanceFeesSentMilliSat", is("4")))
                .andExpect(jsonPath("$.rebalanceReceivedMilliSat", is("61000")))
                .andExpect(jsonPath("$.rebalanceSupportSentMilliSat", is("9000")))
                .andExpect(jsonPath("$.rebalanceSupportFeesSentMilliSat", is("2")))
                .andExpect(jsonPath("$.rebalanceSupportReceivedMilliSat", is("10")))
                .andExpect(jsonPath("$.receivedViaPaymentsMilliSat", is("1")))
                .andExpect(jsonPath("$.totalSentMilliSat", is("70006")))
                .andExpect(jsonPath("$.totalReceivedMilliSat", is("63021")));
    }

    @Test
    void getFlowReport_for_channel_with_max_age() throws Exception {
        when(flowService.getFlowReportForChannel(CHANNEL_ID, Duration.ofDays(1))).thenReturn(FLOW_REPORT);
        mockMvc.perform(get(CHANNEL_PREFIX + "/flow-report/last-days/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getFlowReport_for_peer() throws Exception {
        when(flowService.getFlowReportForPeer(PUBKEY)).thenReturn(FLOW_REPORT);
        mockMvc.perform(get(NODE_PREFIX + "/flow-report"))
                .andExpect(jsonPath("$.forwardedSentMilliSat", is("1050000")))
                .andExpect(jsonPath("$.forwardedReceivedMilliSat", is("9001000")))
                .andExpect(jsonPath("$.forwardingFeesReceivedMilliSat", is("1")))
                .andExpect(jsonPath("$.rebalanceSentMilliSat", is("50000")))
                .andExpect(jsonPath("$.rebalanceFeesSentMilliSat", is("5")))
                .andExpect(jsonPath("$.rebalanceReceivedMilliSat", is("51000")))
                .andExpect(jsonPath("$.rebalanceSupportSentMilliSat", is("123")))
                .andExpect(jsonPath("$.rebalanceSupportFeesSentMilliSat", is("1")))
                .andExpect(jsonPath("$.rebalanceSupportReceivedMilliSat", is("456")))
                .andExpect(jsonPath("$.receivedViaPaymentsMilliSat", is("1500")))
                .andExpect(jsonPath("$.totalSentMilliSat", is("1100129")))
                .andExpect(jsonPath("$.totalReceivedMilliSat", is("9053957")));
    }

    @Test
    void getFlowReport_for_peer_with_max_age() throws Exception {
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(1))).thenReturn(FLOW_REPORT);
        mockMvc.perform(get(NODE_PREFIX + "/flow-report/last-days/1"))
                .andExpect(status().isOk());
    }
}
