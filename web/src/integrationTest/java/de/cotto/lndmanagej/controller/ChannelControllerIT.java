package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import de.cotto.lndmanagej.service.PolicyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_RECEIVED_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.TOTAL_SENT_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PolicyFixtures.POLICIES;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChannelController.class)
class ChannelControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();
    private static final String DETAILS_PREFIX = CHANNEL_PREFIX + "/details";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private NodeService nodeService;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @MockBean
    private OnChainCostService onChainCostService;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private PolicyService policyService;

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
    void getChannelDetails_not_found() throws Exception {
        mockMvc.perform(get(DETAILS_PREFIX)).andExpect(status().isNotFound());
    }

    @Test
    void getChannelDetails() throws Exception {
        when(policyService.getPolicies(CHANNEL_ID)).thenReturn(POLICIES);
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
        when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(1000)));
        when(onChainCostService.getCloseCosts(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(2000)));
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.of(BALANCE_INFORMATION_2));
        mockMvc.perform(get(DETAILS_PREFIX))
                .andExpect(jsonPath("$.channelIdShort", is(String.valueOf(CHANNEL_ID.getShortChannelId()))))
                .andExpect(jsonPath("$.channelIdCompact", is(CHANNEL_ID.getCompactForm())))
                .andExpect(jsonPath("$.channelIdCompactLnd", is(CHANNEL_ID.getCompactFormLnd())))
                .andExpect(jsonPath("$.channelPoint", is(CHANNEL_POINT.toString())))
                .andExpect(jsonPath("$.remotePubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.remoteAlias", is(ALIAS_2)))
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
                .andExpect(jsonPath("$.balance.localBalance", is("2000")))
                .andExpect(jsonPath("$.balance.localReserve", is("200")))
                .andExpect(jsonPath("$.balance.localAvailable", is("1800")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("223")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("20")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("203")))
                .andExpect(jsonPath("$.policies.local.enabled", is(false)))
                .andExpect(jsonPath("$.policies.remote.enabled", is(true)))
                .andExpect(jsonPath("$.policies.local.feeRatePpm", is(100)))
                .andExpect(jsonPath("$.policies.local.baseFeeMilliSat", is(10)))
                .andExpect(jsonPath("$.policies.remote.feeRatePpm", is(222)))
                .andExpect(jsonPath("$.policies.remote.baseFeeMilliSat", is(0)));
    }

    @Test
    void getChannelDetails_closed_channel() throws Exception {
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(CLOSED_CHANNEL));
        mockMvc.perform(get(DETAILS_PREFIX))
                .andExpect(jsonPath("$.totalSent", is("0")))
                .andExpect(jsonPath("$.totalReceived", is("0")))
                .andExpect(jsonPath("$.status.active", is(false)))
                .andExpect(jsonPath("$.status.closed", is(true)))
                .andExpect(jsonPath("$.status.openClosed", is("CLOSED")))
                .andExpect(jsonPath("$.balance.localBalance", is("0")))
                .andExpect(jsonPath("$.balance.localReserve", is("0")))
                .andExpect(jsonPath("$.balance.localAvailable", is("0")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("0")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("0")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("0")))
                .andExpect(jsonPath("$.policies.local.enabled", is(false)))
                .andExpect(jsonPath("$.policies.remote.enabled", is(false)))
                .andExpect(jsonPath("$.closeDetails.initiator", is("REMOTE")))
                .andExpect(jsonPath("$.closeDetails.height", is(987_654)));
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
                .andExpect(jsonPath("$.height", is(987_654)));
    }
}