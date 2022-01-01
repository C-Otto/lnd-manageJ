package de.cotto.lndmanagej.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@WebMvcTest(controllers = ChannelController.class)
class ChannelControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();
    private static final String DETAILS_PREFIX = CHANNEL_PREFIX + "/details";
    private static final FeeReport FEE_REPORT = new FeeReport(Coins.ofMilliSatoshis(1_234), Coins.ofMilliSatoshis(567));

    @Autowired
    private MockMvc mockMvc;

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
    void getBasicInformation_not_found() throws Exception {
        mockMvc.perform(get(CHANNEL_PREFIX + "/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBasicInformation() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_2));
        mockMvc.perform(get(CHANNEL_PREFIX + "/"))
                .andExpect(jsonPath("$.channelIdShort", is(String.valueOf(CHANNEL_ID_2.getShortChannelId()))))
                .andExpect(jsonPath("$.channelIdCompact", is(CHANNEL_ID_2.getCompactForm())))
                .andExpect(jsonPath("$.channelIdCompactLnd", is(CHANNEL_ID_2.getCompactFormLnd())))
                .andExpect(jsonPath("$.channelPoint", is(CHANNEL_POINT.toString())))
                .andExpect(jsonPath("$.remotePubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.capacity", is(String.valueOf(CAPACITY.satoshis()))))
                .andExpect(jsonPath("$.totalSent", is(String.valueOf(TOTAL_SENT_2.satoshis()))))
                .andExpect(jsonPath("$.totalReceived", is(String.valueOf(TOTAL_RECEIVED_2.satoshis()))))
                .andExpect(jsonPath("$.openInitiator", is("REMOTE")))
                .andExpect(jsonPath("$.openHeight", is(CHANNEL_ID_2.getBlockHeight())))
                .andExpect(jsonPath("$.status.private", is(false)))
                .andExpect(jsonPath("$.status.active", is(false)))
                .andExpect(jsonPath("$.status.closed", is(false)))
                .andExpect(jsonPath("$.status.openClosed", is("OPEN")));
    }

    @Test
    void getBasicInformation_closed_channel() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        mockMvc.perform(get(CHANNEL_PREFIX + "/"))
                .andExpect(jsonPath("$.totalSent", is("0")))
                .andExpect(jsonPath("$.totalReceived", is("0")))
                .andExpect(jsonPath("$.closeDetails.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.closeDetails.height", is(600_000)))
                .andExpect(jsonPath("$.status.active", is(false)))
                .andExpect(jsonPath("$.status.closed", is(true)))
                .andExpect(jsonPath("$.status.openClosed", is("CLOSED")));
    }

    @Test
    void getChannelDetails_not_found() throws Exception {
        mockMvc.perform(get(DETAILS_PREFIX)).andExpect(status().isNotFound());
    }

    @Test
    void getChannelDetails() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
        when(channelDetailsService.getDetails(LOCAL_OPEN_CHANNEL_PRIVATE)).thenReturn(CHANNEL_DETAILS_2);
        mockMvc.perform(get(DETAILS_PREFIX))
                .andExpect(jsonPath("$.channelIdShort", is(String.valueOf(CHANNEL_ID.getShortChannelId()))))
                .andExpect(jsonPath("$.channelIdCompact", is(CHANNEL_ID.getCompactForm())))
                .andExpect(jsonPath("$.channelIdCompactLnd", is(CHANNEL_ID.getCompactFormLnd())))
                .andExpect(jsonPath("$.channelPoint", is(CHANNEL_POINT.toString())))
                .andExpect(jsonPath("$.remotePubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.remoteAlias", is(ALIAS)))
                .andExpect(jsonPath("$.capacity", is(String.valueOf(CAPACITY.satoshis()))))
                .andExpect(jsonPath("$.totalSent", is(String.valueOf(TOTAL_SENT.satoshis()))))
                .andExpect(jsonPath("$.totalReceived", is(String.valueOf(TOTAL_RECEIVED.satoshis()))))
                .andExpect(jsonPath("$.openInitiator", is("LOCAL")))
                .andExpect(jsonPath("$.openHeight", is(CHANNEL_ID.getBlockHeight())))
                .andExpect(jsonPath("$.status.private", is(true)))
                .andExpect(jsonPath("$.status.active", is(true)))
                .andExpect(jsonPath("$.status.closed", is(false)))
                .andExpect(jsonPath("$.status.openClosed", is("OPEN")))
                .andExpect(jsonPath("$.onChainCosts.openCosts", is("1000")))
                .andExpect(jsonPath("$.onChainCosts.closeCosts", is("2000")))
                .andExpect(jsonPath("$.onChainCosts.sweepCosts", is("3000")))
                .andExpect(jsonPath("$.rebalanceReport.sourceCosts", is("1001000")))
                .andExpect(jsonPath("$.rebalanceReport.sourceAmount", is("666000")))
                .andExpect(jsonPath("$.rebalanceReport.targetCosts", is("2001000")))
                .andExpect(jsonPath("$.rebalanceReport.targetAmount", is("992000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsSourceAmount", is("101000")))
                .andExpect(jsonPath("$.rebalanceReport.supportAsTargetAmount", is("201000")))
                .andExpect(jsonPath("$.balance.localBalance", is("1000")))
                .andExpect(jsonPath("$.balance.localReserve", is("100")))
                .andExpect(jsonPath("$.balance.localAvailable", is("900")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("123")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("10")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("113")))
                .andExpect(jsonPath("$.policies.local.enabled", is(false)))
                .andExpect(jsonPath("$.policies.remote.enabled", is(true)))
                .andExpect(jsonPath("$.policies.local.feeRatePpm", is(100)))
                .andExpect(jsonPath("$.policies.local.baseFeeMilliSat", is(10)))
                .andExpect(jsonPath("$.policies.remote.feeRatePpm", is(222)))
                .andExpect(jsonPath("$.policies.remote.baseFeeMilliSat", is(0)))
                .andExpect(jsonPath("$.feeReport.earned", is("1234")))
                .andExpect(jsonPath("$.feeReport.sourced", is("567")))
                .andExpect(jsonPath("$.flowReport.forwardedSent", is("1000")))
                .andExpect(jsonPath("$.flowReport.forwardedReceived", is("2000")))
                .andExpect(jsonPath("$.flowReport.forwardingFeesReceived", is("10")))
                .andExpect(jsonPath("$.flowReport.rebalanceSent", is("60000")))
                .andExpect(jsonPath("$.flowReport.rebalanceFeesSent", is("4")))
                .andExpect(jsonPath("$.flowReport.rebalanceReceived", is("61000")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportSent", is("9000")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportFeesSent", is("2")))
                .andExpect(jsonPath("$.flowReport.rebalanceSupportReceived", is("10")))
                .andExpect(jsonPath("$.flowReport.totalSent", is("70006")))
                .andExpect(jsonPath("$.flowReport.totalReceived", is("63020")));
    }

    @Test
    void getChannelDetails_closed_channel() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        when(channelDetailsService.getDetails(CLOSED_CHANNEL)).thenReturn(CHANNEL_DETAILS_CLOSED);
        mockMvc.perform(get(DETAILS_PREFIX))
                .andExpect(jsonPath("$.closeDetails.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.closeDetails.height", is(600_000)))
                .andExpect(jsonPath("$.status.openClosed", is("CLOSED")))
                .andExpect(jsonPath("$.totalSent", is("0")))
                .andExpect(jsonPath("$.totalReceived", is("0")))
                .andExpect(jsonPath("$.status.active", is(false)))
                .andExpect(jsonPath("$.status.closed", is(true)))
                .andExpect(jsonPath("$.balance.localBalance", is("0")))
                .andExpect(jsonPath("$.balance.localReserve", is("0")))
                .andExpect(jsonPath("$.balance.localAvailable", is("0")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("0")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("0")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("0")))
                .andExpect(jsonPath("$.policies.local.enabled", is(false)))
                .andExpect(jsonPath("$.policies.remote.enabled", is(false)));
    }

    @Test
    void getChannelDetails_channel_not_found() throws Exception {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        mockMvc.perform(get(DETAILS_PREFIX))
                .andExpect(status().isNotFound());
    }

    @Test
    void getBalance() throws Exception {
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.of(BALANCE_INFORMATION_2));
        mockMvc.perform(get(CHANNEL_PREFIX + "/balance"))
                .andExpect(jsonPath("$.localBalance", is("2000")))
                .andExpect(jsonPath("$.localReserve", is("200")))
                .andExpect(jsonPath("$.localAvailable", is("1800")))
                .andExpect(jsonPath("$.remoteBalance", is("223")))
                .andExpect(jsonPath("$.remoteReserve", is("20")))
                .andExpect(jsonPath("$.remoteAvailable", is("203")));
    }

    @Test
    void getPolicies() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL));
        when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
        mockMvc.perform(get(CHANNEL_PREFIX + "/policies"))
                .andExpect(jsonPath("$.local.feeRatePpm", is(100)))
                .andExpect(jsonPath("$.local.baseFeeMilliSat", is(10)))
                .andExpect(jsonPath("$.remote.feeRatePpm", is(222)))
                .andExpect(jsonPath("$.remote.baseFeeMilliSat", is(0)))
                .andExpect(jsonPath("$.local.enabled", is(false)))
                .andExpect(jsonPath("$.remote.enabled", is(true)));
    }

    @Test
    void getCloseDetails() throws Exception {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        mockMvc.perform(get(CHANNEL_PREFIX + "/close-details"))
                .andExpect(jsonPath("$.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.height", is(600_000)))
                .andExpect(jsonPath("$.force", is(false)))
                .andExpect(jsonPath("$.breach", is(false)));
    }

    @Test
    void getCloseDetails_force() throws Exception {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL));
        mockMvc.perform(get(CHANNEL_PREFIX + "/close-details"))
                .andExpect(jsonPath("$.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.height", is(600_000)))
                .andExpect(jsonPath("$.force", is(true)))
                .andExpect(jsonPath("$.breach", is(false)));
    }

    @Test
    void getCloseDetails_breach() throws Exception {
        when(channelService.getClosedChannel(CHANNEL_ID)).thenReturn(Optional.of(FORCE_CLOSED_CHANNEL_BREACH));
        mockMvc.perform(get(CHANNEL_PREFIX + "/close-details"))
                .andExpect(jsonPath("$.breach", is(true)))
                .andExpect(jsonPath("$.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.height", is(600_000)))
                .andExpect(jsonPath("$.force", is(true)));
    }

    @Test
    void getFeeReport() throws Exception {
        when(feeService.getFeeReportForChannel(CHANNEL_ID)).thenReturn(FEE_REPORT);
        mockMvc.perform(get(CHANNEL_PREFIX + "/fee-report"))
                .andExpect(jsonPath("$.earned", is("1234")))
                .andExpect(jsonPath("$.sourced", is("567")));
    }
}