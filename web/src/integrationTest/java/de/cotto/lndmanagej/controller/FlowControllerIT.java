package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.service.FlowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT;
import static de.cotto.lndmanagej.model.FlowReportFixtures.FLOW_REPORT_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(FlowController.class)
@Import(ChannelIdParser.class)
class FlowControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FlowService flowService;

    @MockitoBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void getFlowReport_for_channel() {
        when(flowService.getFlowReportForChannel(CHANNEL_ID)).thenReturn(FLOW_REPORT_2);
        webTestClient.get().uri(CHANNEL_PREFIX + "/flow-report").exchange()
                .expectBody()
                .jsonPath("$.forwardedSentMilliSat").value(v -> assertThat(v).isEqualTo("1000"))
                .jsonPath("$.forwardedReceivedMilliSat").value(v -> assertThat(v).isEqualTo("2000"))
                .jsonPath("$.forwardingFeesReceivedMilliSat").value(v -> assertThat(v).isEqualTo("10"))
                .jsonPath("$.rebalanceSentMilliSat").value(v -> assertThat(v).isEqualTo("60000"))
                .jsonPath("$.rebalanceFeesSentMilliSat").value(v -> assertThat(v).isEqualTo("4"))
                .jsonPath("$.rebalanceReceivedMilliSat").value(v -> assertThat(v).isEqualTo("61000"))
                .jsonPath("$.rebalanceSupportSentMilliSat").value(v -> assertThat(v).isEqualTo("9000"))
                .jsonPath("$.rebalanceSupportFeesSentMilliSat").value(v -> assertThat(v).isEqualTo("2"))
                .jsonPath("$.rebalanceSupportReceivedMilliSat").value(v -> assertThat(v).isEqualTo("10"))
                .jsonPath("$.receivedViaPaymentsMilliSat").value(v -> assertThat(v).isEqualTo("1"))
                .jsonPath("$.totalSentMilliSat").value(v -> assertThat(v).isEqualTo("70006"))
                .jsonPath("$.totalReceivedMilliSat").value(v -> assertThat(v).isEqualTo("63021"));
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
                .jsonPath("$.forwardedSentMilliSat").value(v -> assertThat(v).isEqualTo("1050000"))
                .jsonPath("$.forwardedReceivedMilliSat").value(v -> assertThat(v).isEqualTo("9001000"))
                .jsonPath("$.forwardingFeesReceivedMilliSat").value(v -> assertThat(v).isEqualTo("1"))
                .jsonPath("$.rebalanceSentMilliSat").value(v -> assertThat(v).isEqualTo("50000"))
                .jsonPath("$.rebalanceFeesSentMilliSat").value(v -> assertThat(v).isEqualTo("5"))
                .jsonPath("$.rebalanceReceivedMilliSat").value(v -> assertThat(v).isEqualTo("51000"))
                .jsonPath("$.rebalanceSupportSentMilliSat").value(v -> assertThat(v).isEqualTo("123"))
                .jsonPath("$.rebalanceSupportFeesSentMilliSat").value(v -> assertThat(v).isEqualTo("1"))
                .jsonPath("$.rebalanceSupportReceivedMilliSat").value(v -> assertThat(v).isEqualTo("456"))
                .jsonPath("$.receivedViaPaymentsMilliSat").value(v -> assertThat(v).isEqualTo("1500"))
                .jsonPath("$.totalSentMilliSat").value(v -> assertThat(v).isEqualTo("1100129"))
                .jsonPath("$.totalReceivedMilliSat").value(v -> assertThat(v).isEqualTo("9053957"));
    }

    @Test
    void getFlowReport_for_peer_with_max_age() {
        when(flowService.getFlowReportForPeer(PUBKEY, Duration.ofDays(1))).thenReturn(FLOW_REPORT);
        webTestClient.get().uri(NODE_PREFIX + "/flow-report/last-days/1").exchange()
                .expectStatus().isOk();
    }
}
