package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
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
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

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

@WebFluxTest(NodeController.class)
@Import(ChannelIdParser.class)
class NodeControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Autowired
    private WebTestClient webTestClient;

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
    void getAlias() {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS_2);
        webTestClient.get().uri(NODE_PREFIX + "/alias").exchange()
                .expectBody(String.class).isEqualTo(ALIAS_2);
    }

    @Test
    void getDetails() {
        when(nodeDetailsService.getDetails(PUBKEY)).thenReturn(NODE_DETAILS);
        List<String> channelIds = List.of(CHANNEL_ID.toString());
        List<String> closedChannelIds = List.of(CHANNEL_ID_2.toString());
        List<String> waitingCloseChannelIds = List.of(CHANNEL_ID_3.toString());
        List<String> forceClosingChannelIds = List.of(CHANNEL_ID_4.toString());
        webTestClient.get().uri(NODE_PREFIX + "/details").exchange().expectBody()
                .jsonPath("$.node").value(is(PUBKEY.toString()))
                .jsonPath("$.alias").value(is(ALIAS))
                .jsonPath("$.channels").value(is(channelIds))
                .jsonPath("$.closedChannels").value(is(closedChannelIds))
                .jsonPath("$.waitingCloseChannels").value(is(waitingCloseChannelIds))
                .jsonPath("$.pendingForceClosingChannels").value(is(forceClosingChannelIds))
                .jsonPath("$.rebalanceReport.sourceCostsMilliSat").value(is("1000000"))
                .jsonPath("$.rebalanceReport.targetCostsMilliSat").value(is("2000000"))
                .jsonPath("$.rebalanceReport.sourceAmountMilliSat").value(is("665000"))
                .jsonPath("$.rebalanceReport.targetAmountMilliSat").value(is("991000"))
                .jsonPath("$.rebalanceReport.supportAsSourceAmountMilliSat").value(is("100000"))
                .jsonPath("$.rebalanceReport.supportAsTargetAmountMilliSat").value(is("200000"))
                .jsonPath("$.flowReport.forwardedSentMilliSat").value(is("1050000"))
                .jsonPath("$.flowReport.forwardedReceivedMilliSat").value(is("9001000"))
                .jsonPath("$.flowReport.forwardingFeesReceivedMilliSat").value(is("1"))
                .jsonPath("$.flowReport.rebalanceSentMilliSat").value(is("50000"))
                .jsonPath("$.flowReport.rebalanceFeesSentMilliSat").value(is("5"))
                .jsonPath("$.flowReport.rebalanceReceivedMilliSat").value(is("51000"))
                .jsonPath("$.flowReport.rebalanceSupportSentMilliSat").value(is("123"))
                .jsonPath("$.flowReport.rebalanceSupportFeesSentMilliSat").value(is("1"))
                .jsonPath("$.flowReport.rebalanceSupportReceivedMilliSat").value(is("456"))
                .jsonPath("$.flowReport.receivedViaPaymentsMilliSat").value(is("1500"))
                .jsonPath("$.flowReport.totalSentMilliSat").value(is("1100129"))
                .jsonPath("$.flowReport.totalReceivedMilliSat").value(is("9053957"))
                .jsonPath("$.balance.localBalanceSat").value(is("2000"))
                .jsonPath("$.balance.localReserveSat").value(is("200"))
                .jsonPath("$.balance.localAvailableSat").value(is("1800"))
                .jsonPath("$.balance.remoteBalanceSat").value(is("223"))
                .jsonPath("$.balance.remoteReserveSat").value(is("20"))
                .jsonPath("$.balance.remoteAvailableSat").value(is("203"))
                .jsonPath("$.feeReport.earnedMilliSat").value(is("1234"))
                .jsonPath("$.feeReport.sourcedMilliSat").value(is("567"))
                .jsonPath("$.warnings").value(containsInAnyOrder(
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days",
                        "No flow in the past 16 days"
                ))
                .jsonPath("$.onChainCosts.openCostsSat").value(is("1000"))
                .jsonPath("$.onChainCosts.closeCostsSat").value(is("2000"))
                .jsonPath("$.onChainCosts.sweepCostsSat").value(is("3000"))
                .jsonPath("$.onlineReport.online").value(is(true))
                .jsonPath("$.onlineReport.onlinePercentage").value(is(77))
                .jsonPath("$.onlineReport.daysForOnlinePercentage").value(is(14))
                .jsonPath("$.onlineReport.changes").value(is(5))
                .jsonPath("$.onlineReport.daysForChanges").value(is(7))
                .jsonPath("$.onlineReport.since").value(is("2021-12-23T01:02:03Z"))
                .jsonPath("$.rating.rating").value(is(123))
                .jsonPath("$.rating.message").value(is(""));
    }

    @Test
    void getOpenChannelIds() {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        webTestClient.get().uri(NODE_PREFIX + "/open-channels").exchange().expectBody()
                .jsonPath("$.node").value(is(PUBKEY.toString()))
                .jsonPath("$.channels").value(is(channelIds));
    }

    @Test
    void getAllChannelIds() {
        when(channelService.getAllChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        webTestClient.get().uri(NODE_PREFIX + "/all-channels").exchange().expectBody()
                .jsonPath("$.node").value(is(PUBKEY.toString()))
                .jsonPath("$.channels").value(is(channelIds));
    }

    @Test
    void getBalance() {
        when(balanceService.getBalanceInformationForPeer(PUBKEY)).thenReturn(BALANCE_INFORMATION);
        webTestClient.get().uri(NODE_PREFIX + "/balance").exchange().expectBody()
                .jsonPath("$.localBalanceSat").value(is("1000"))
                .jsonPath("$.localReserveSat").value(is("100"))
                .jsonPath("$.localAvailableSat").value(is("900"))
                .jsonPath("$.remoteBalanceSat").value(is("123"))
                .jsonPath("$.remoteReserveSat").value(is("10"))
                .jsonPath("$.remoteAvailableSat").value(is("113"));
    }

    @Test
    void getFeeReport() {
        when(feeService.getFeeReportForPeer(PUBKEY)).thenReturn(FEE_REPORT);
        webTestClient.get().uri(NODE_PREFIX + "/fee-report").exchange().expectBody()
                .jsonPath("$.earnedMilliSat").value(is("1234"))
                .jsonPath("$.sourcedMilliSat").value(is("567"));
    }

    @Test
    void getFeeReport_last_days() {
        when(feeService.getFeeReportForPeer(PUBKEY, Duration.ofDays(123))).thenReturn(FEE_REPORT);
        webTestClient.get().uri(NODE_PREFIX + "/fee-report/last-days/123").exchange().expectBody()
                .jsonPath("$.earnedMilliSat").value(is("1234"))
                .jsonPath("$.sourcedMilliSat").value(is("567"));
    }
}
