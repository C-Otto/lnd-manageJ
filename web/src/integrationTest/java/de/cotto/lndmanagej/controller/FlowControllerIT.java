package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.FlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@WebFluxTest(FlowController.class)
@Import(ChannelIdParser.class)
class FlowControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FlowService flowService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void getFlowReport_for_channel() {
        when(flowService.getFlowReportForChannel(CHANNEL_ID)).thenReturn(FLOW_REPORT_2);
        webTestClient.get().uri(CHANNEL_PREFIX + "/flow-report").exchange()
                .expectBody()
                .jsonPath("$.forwardedSentMilliSat").value(is("1000"))
                .jsonPath("$.forwardedReceivedMilliSat").value(is("2000"))
                .jsonPath("$.forwardingFeesReceivedMilliSat").value(is("10"))
                .jsonPath("$.rebalanceSentMilliSat").value(is("60000"))
                .jsonPath("$.rebalanceFeesSentMilliSat").value(is("4"))
                .jsonPath("$.rebalanceReceivedMilliSat").value(is("61000"))
                .jsonPath("$.rebalanceSupportSentMilliSat").value(is("9000"))
                .jsonPath("$.rebalanceSupportFeesSentMilliSat").value(is("2"))
                .jsonPath("$.rebalanceSupportReceivedMilliSat").value(is("10"))
                .jsonPath("$.receivedViaPaymentsMilliSat").value(is("1"))
                .jsonPath("$.totalSentMilliSat").value(is("70006"))
                .jsonPath("$.totalReceivedMilliSat").value(is("63021"));
    }

    @Test
    void getFlowReport_for_channel_with_max_age() {
        when(flowService.getFlowReportForChannel(CHANNEL_ID, Duration.ofDays(1))).thenReturn(FLOW_REPORT);
        webTestClient.get().uri(CHANNEL_PREFIX + "/flow-report/last-days/1").exchange()
                .expectStatus().isOk();
    }

    @Test
    void getFlowReport_for_peer() {
        when(flowService.getFlowReportForPeer(PUBKEY)).thenReturn(FLOW_REPORT);
        webTestClient.get().uri(NODE_PREFIX + "/flow-report").exchange()
                .expectBody()
                .jsonPath("$.forwardedSentMilliSat").value(is("1050000"))
                .jsonPath("$.forwardedReceivedMilliSat").value(is("9001000"))
                .jsonPath("$.forwardingFeesReceivedMilliSat").value(is("1"))
                .jsonPath("$.rebalanceSentMilliSat").value(is("50000"))
                .jsonPath("$.rebalanceFeesSentMilliSat").value(is("5"))
                .jsonPath("$.rebalanceReceivedMilliSat").value(is("51000"))
                .jsonPath("$.rebalanceSupportSentMilliSat").value(is("123"))
                .jsonPath("$.rebalanceSupportFeesSentMilliSat").value(is("1"))
                .jsonPath("$.rebalanceSupportReceivedMilliSat").value(is("456"))
                .jsonPath("$.receivedViaPaymentsMilliSat").value(is("1500"))
                .jsonPath("$.totalSentMilliSat").value(is("1100129"))
                .jsonPath("$.totalReceivedMilliSat").value(is("9053957"));
    }

    @Test
    void getFlowReport_for_peer_with_max_age() {
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(1))).thenReturn(FLOW_REPORT);
        webTestClient.get().uri(NODE_PREFIX + "/flow-report/last-days/1").exchange()
                .expectStatus().isOk();
    }
}
