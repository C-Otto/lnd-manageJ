package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.FeeReport;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelDetailsService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS_2;
import static de.cotto.lndmanagej.model.ChannelDetailsFixtures.CHANNEL_DETAILS_CLOSED;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.ForceClosedChannelFixtures.FORCE_CLOSED_CHANNEL_BREACH;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.NUM_UPDATES;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES_FOR_LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@WebFluxTest(ChannelController.class)
@Import(ChannelIdParser.class)
class ChannelControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();
    private static final String DETAILS_PREFIX = CHANNEL_PREFIX + "/details";
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private NodeService nodeService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private FeeService feeService;

    @MockBean
    private ChannelDetailsService channelDetailsService;

    @Test
    void getBasicInformation_not_found() {
        webTestClient.get().uri(CHANNEL_PREFIX + "/")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getBasicInformation() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
        webTestClient.get().uri(CHANNEL_PREFIX + "/")
                .exchange()
                .expectBody()
                .jsonPath("$.channelIdShort").value(is(String.valueOf(CHANNEL_ID_2.getShortChannelId())))
                .jsonPath("$.channelIdCompact").value(is(CHANNEL_ID_2.getCompactForm()))
                .jsonPath("$.channelIdCompactLnd").value(is(CHANNEL_ID_2.getCompactFormLnd()))
                .jsonPath("$.channelPoint").value(is(CHANNEL_POINT.toString()))
                .jsonPath("$.remotePubkey").value(is(PUBKEY_2.toString()))
                .jsonPath("$.capacitySat").value(is(String.valueOf(CAPACITY.satoshis())))
                .jsonPath("$.totalSentSat").value(is(String.valueOf(TOTAL_SENT_2.satoshis())))
                .jsonPath("$.totalReceivedSat").value(is(String.valueOf(TOTAL_RECEIVED_2.satoshis())))
                .jsonPath("$.openInitiator").value(is("REMOTE"))
                .jsonPath("$.openHeight").value(is(CHANNEL_ID_2.getBlockHeight()))
                .jsonPath("$.status.private").value(is(false))
                .jsonPath("$.status.active").value(is(false))
                .jsonPath("$.status.closed").value(is(false))
                .jsonPath("$.status.openClosed").value(is("OPEN"))
                .jsonPath("$.numUpdates").value(is(NUM_UPDATES));
    }

    @Test
    void getBasicInformation_closed_channel() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        webTestClient.get().uri(CHANNEL_PREFIX + "/")
                .exchange()
                .expectBody()
                .jsonPath("$.totalSentSat").value(is("0"))
                .jsonPath("$.totalReceivedSat").value(is("0"))
                .jsonPath("$.closeDetails.initiator").value(is("REMOTE"))
                .jsonPath("$.closeDetails.height").value(is(600_000))
                .jsonPath("$.status.active").value(is(false))
                .jsonPath("$.status.closed").value(is(true))
                .jsonPath("$.status.openClosed").value(is("CLOSED"));
    }

    @Test
    void getChannelDetails_not_found() {
        webTestClient.get().uri(DETAILS_PREFIX).exchange().expectStatus().isNotFound();
    }

    @Test
    void getChannelDetails() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
        when(channelDetailsService.getDetails(LOCAL_OPEN_CHANNEL_PRIVATE)).thenReturn(CHANNEL_DETAILS_2);
        webTestClient.get().uri(DETAILS_PREFIX)
                .exchange()
                .expectBody()
                .jsonPath("$.channelIdShort").value(is(String.valueOf(CHANNEL_ID.getShortChannelId())))
                .jsonPath("$.channelIdCompact").value(is(CHANNEL_ID.getCompactForm()))
                .jsonPath("$.channelIdCompactLnd").value(is(CHANNEL_ID.getCompactFormLnd()))
                .jsonPath("$.channelPoint").value(is(CHANNEL_POINT.toString()))
                .jsonPath("$.remotePubkey").value(is(PUBKEY_2.toString()))
                .jsonPath("$.remoteAlias").value(is(ALIAS))
                .jsonPath("$.capacitySat").value(is(String.valueOf(CAPACITY.satoshis())))
                .jsonPath("$.totalSentSat").value(is(String.valueOf(TOTAL_SENT.satoshis())))
                .jsonPath("$.totalReceivedSat").value(is(String.valueOf(TOTAL_RECEIVED.satoshis())))
                .jsonPath("$.openInitiator").value(is("LOCAL"))
                .jsonPath("$.openHeight").value(is(CHANNEL_ID.getBlockHeight()))
                .jsonPath("$.status.private").value(is(true))
                .jsonPath("$.status.active").value(is(true))
                .jsonPath("$.status.closed").value(is(false))
                .jsonPath("$.status.openClosed").value(is("OPEN"))
                .jsonPath("$.numUpdates").value(is(NUM_UPDATES))
                .jsonPath("$.onChainCosts.openCostsSat").value(is("1000"))
                .jsonPath("$.onChainCosts.closeCostsSat").value(is("2000"))
                .jsonPath("$.onChainCosts.sweepCostsSat").value(is("3000"))
                .jsonPath("$.rebalanceReport.sourceCostsMilliSat").value(is("1001000"))
                .jsonPath("$.rebalanceReport.sourceAmountMilliSat").value(is("666000"))
                .jsonPath("$.rebalanceReport.targetCostsMilliSat").value(is("2001000"))
                .jsonPath("$.rebalanceReport.targetAmountMilliSat").value(is("992000"))
                .jsonPath("$.rebalanceReport.supportAsSourceAmountMilliSat").value(is("101000"))
                .jsonPath("$.rebalanceReport.supportAsTargetAmountMilliSat").value(is("201000"))
                .jsonPath("$.balance.localBalanceSat").value(is("1000"))
                .jsonPath("$.balance.localReserveSat").value(is("100"))
                .jsonPath("$.balance.localAvailableSat").value(is("900"))
                .jsonPath("$.balance.remoteBalanceSat").value(is("123"))
                .jsonPath("$.balance.remoteReserveSat").value(is("10"))
                .jsonPath("$.balance.remoteAvailableSat").value(is("113"))
                .jsonPath("$.policies.local.enabled").value(is(false))
                .jsonPath("$.policies.local.feeRatePpm").value(is(200))
                .jsonPath("$.policies.local.baseFeeMilliSat").value(is("0"))
                .jsonPath("$.policies.local.timeLockDelta").value(is(40))
                .jsonPath("$.policies.local.minHtlcMilliSat").value(is("0"))
                .jsonPath("$.policies.local.maxHtlcMilliSat").value(is("0"))
                .jsonPath("$.policies.remote.enabled").value(is(true))
                .jsonPath("$.policies.remote.feeRatePpm").value(is(300))
                .jsonPath("$.policies.remote.baseFeeMilliSat").value(is("0"))
                .jsonPath("$.policies.remote.timeLockDelta").value(is(144))
                .jsonPath("$.policies.remote.minHtlcMilliSat").value(is("159000"))
                .jsonPath("$.policies.remote.maxHtlcMilliSat").value(is("22222000"))
                .jsonPath("$.feeReport.earnedMilliSat").value(is("1234"))
                .jsonPath("$.feeReport.sourcedMilliSat").value(is("567"))
                .jsonPath("$.flowReport.forwardedSentMilliSat").value(is("1000"))
                .jsonPath("$.flowReport.forwardedReceivedMilliSat").value(is("2000"))
                .jsonPath("$.flowReport.forwardingFeesReceivedMilliSat").value(is("10"))
                .jsonPath("$.flowReport.rebalanceSentMilliSat").value(is("60000"))
                .jsonPath("$.flowReport.rebalanceFeesSentMilliSat").value(is("4"))
                .jsonPath("$.flowReport.rebalanceReceivedMilliSat").value(is("61000"))
                .jsonPath("$.flowReport.rebalanceSupportSentMilliSat").value(is("9000"))
                .jsonPath("$.flowReport.rebalanceSupportFeesSentMilliSat").value(is("2"))
                .jsonPath("$.flowReport.rebalanceSupportReceivedMilliSat").value(is("10"))
                .jsonPath("$.flowReport.receivedViaPaymentsMilliSat").value(is("1"))
                .jsonPath("$.flowReport.totalSentMilliSat").value(is("70006"))
                .jsonPath("$.flowReport.totalReceivedMilliSat").value(is("63021"))
                .jsonPath("$.rating.rating").value(is(123))
                .jsonPath("$.rating.message").value(is(""))
                .jsonPath("$.warnings").value(containsInAnyOrder(
                        "Channel has accumulated 101,000 updates"
                ));
    }

    @Test
    void getChannelDetails_closed_channel() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(channelDetailsService.getDetails(CLOSED_CHANNEL)).thenReturn(CHANNEL_DETAILS_CLOSED);
        webTestClient.get().uri(DETAILS_PREFIX)
                .exchange()
                .expectBody()
                .jsonPath("$.closeDetails.initiator").value(is("REMOTE"))
                .jsonPath("$.closeDetails.height").value(is(600_000))
                .jsonPath("$.status.openClosed").value(is("CLOSED"))
                .jsonPath("$.totalSentSat").value(is("0"))
                .jsonPath("$.totalReceivedSat").value(is("0"))
                .jsonPath("$.status.active").value(is(false))
                .jsonPath("$.status.closed").value(is(true))
                .jsonPath("$.balance.localBalanceSat").value(is("0"))
                .jsonPath("$.balance.localReserveSat").value(is("0"))
                .jsonPath("$.balance.localAvailableSat").value(is("0"))
                .jsonPath("$.balance.remoteBalanceSat").value(is("0"))
                .jsonPath("$.balance.remoteReserveSat").value(is("0"))
                .jsonPath("$.balance.remoteAvailableSat").value(is("0"))
                .jsonPath("$.policies.local.enabled").value(is(false))
                .jsonPath("$.policies.remote.enabled").value(is(false));
    }

    @Test
    void getChannelDetails_channel_not_found() {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        webTestClient.get().uri(DETAILS_PREFIX).exchange().expectStatus().isNotFound();

    }

    @Test
    void getBalance() {
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.of(BALANCE_INFORMATION_2));
        webTestClient.get().uri(CHANNEL_PREFIX + "/balance")
                .exchange()
                .expectBody()
                .jsonPath("$.localBalanceSat").value(is("2000"))
                .jsonPath("$.localReserveSat").value(is("200"))
                .jsonPath("$.localAvailableSat").value(is("1800"))
                .jsonPath("$.remoteBalanceSat").value(is("223"))
                .jsonPath("$.remoteReserveSat").value(is("20"))
                .jsonPath("$.remoteAvailableSat").value(is("203"));
    }

    @Test
    void getPolicies() {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicies(LOCAL_OPEN_CHANNEL)).thenReturn(POLICIES_FOR_LOCAL_CHANNEL);
        webTestClient.get().uri(CHANNEL_PREFIX + "/policies")
                .exchange()
                .expectBody()
                .jsonPath("$.local.feeRatePpm").value(is(200))
                .jsonPath("$.local.baseFeeMilliSat").value(is("0"))
                .jsonPath("$.remote.feeRatePpm").value(is(300))
                .jsonPath("$.remote.baseFeeMilliSat").value(is("0"))
                .jsonPath("$.local.enabled").value(is(false))
                .jsonPath("$.remote.enabled").value(is(true));
    }

    // CPD-OFF
    @Test
    void getCloseDetails() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        webTestClient.get().uri(CHANNEL_PREFIX + "/close-details")
                .exchange()
                .expectBody()
                .jsonPath("$.initiator").value(is("REMOTE"))
                .jsonPath("$.height").value(is(600_000))
                .jsonPath("$.force").value(is(false))
                .jsonPath("$.breach").value(is(false));
    }

    @Test
    void getCloseDetails_force() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL));
        webTestClient.get().uri(CHANNEL_PREFIX + "/close-details")
                .exchange()
                .expectBody()
                .jsonPath("$.initiator").value(is("REMOTE"))
                .jsonPath("$.height").value(is(600_000))
                .jsonPath("$.force").value(is(true))
                .jsonPath("$.breach").value(is(false));
    }
    // CPD-ON

    @Test
    void getCloseDetails_breach() {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_BREACH));
        webTestClient.get().uri(CHANNEL_PREFIX + "/close-details")
                .exchange()
                .expectBody()
                .jsonPath("$.breach").value(is(true))
                .jsonPath("$.initiator").value(is("REMOTE"))
                .jsonPath("$.height").value(is(600_000))
                .jsonPath("$.force").value(is(true));
    }

    @Test
    void getFeeReport() {
        when(feeService.getFeeReportForChannel(CHANNEL_ID)).thenReturn(FEE_REPORT);
        webTestClient.get().uri(CHANNEL_PREFIX + "/fee-report")
                .exchange()
                .expectBody()
                .jsonPath("$.earnedMilliSat").value(is("1234"))
                .jsonPath("$.sourcedMilliSat").value(is("567"));
    }
}
