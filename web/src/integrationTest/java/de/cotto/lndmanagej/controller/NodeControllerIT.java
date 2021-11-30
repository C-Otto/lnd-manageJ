package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_2;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL;
import static de.cotto.lndmanagej.model.CoopClosedChannelFixtures.CLOSED_CHANNEL_3;
import static de.cotto.lndmanagej.model.ForceClosingChannelFixtures.FORCE_CLOSING_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_2;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.WaitingCloseChannelFixtures.WAITING_CLOSE_CHANNEL;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = NodeController.class)
class NodeControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY_2;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private OnChainCostService onChainCostService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private FeeService feeService;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @Test
    void getAlias() throws Exception {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        mockMvc.perform(get(NODE_PREFIX + "/alias"))
                .andExpect(content().string(ALIAS_2));
    }

    @Test
    void getDetails() throws Exception {
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, true));
        when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_2));
        when(channelService.getClosedChannelsWith(PUBKEY_2)).thenReturn(Set.of(CLOSED_CHANNEL, CLOSED_CHANNEL_3));
        when(channelService.getWaitingCloseChannelsFor(PUBKEY_2)).thenReturn(Set.of(WAITING_CLOSE_CHANNEL));
        when(channelService.getForceClosingChannelsFor(PUBKEY_2)).thenReturn(Set.of(FORCE_CLOSING_CHANNEL_2));
        when(onChainCostService.getOpenCostsWith(PUBKEY_2)).thenReturn(Coins.ofSatoshis(123));
        when(onChainCostService.getCloseCostsWith(PUBKEY_2)).thenReturn(Coins.ofSatoshis(456));
        when(balanceService.getBalanceInformation(PUBKEY_2)).thenReturn(BALANCE_INFORMATION);
        when(feeService.getEarnedFeesForPeer(PUBKEY_2)).thenReturn(Coins.ofMilliSatoshis(1_234));
        when(feeService.getSourcedFeesForPeer(PUBKEY_2)).thenReturn(Coins.ofMilliSatoshis(567));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_2.toString());
        List<String> closedChannelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        List<String> waitingCloseChannelIds = List.of(CHANNEL_ID.toString());
        List<String> forceClosingChannelIds = List.of(CHANNEL_ID_2.toString());
        mockMvc.perform(get(NODE_PREFIX + "/details"))
                .andExpect(jsonPath("$.node", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.alias", is(ALIAS_2)))
                .andExpect(jsonPath("$.channels", is(channelIds)))
                .andExpect(jsonPath("$.closedChannels", is(closedChannelIds)))
                .andExpect(jsonPath("$.waitingCloseChannels", is(waitingCloseChannelIds)))
                .andExpect(jsonPath("$.pendingForceClosingChannels", is(forceClosingChannelIds)))
                .andExpect(jsonPath("$.onChainCosts.openCosts", is("123")))
                .andExpect(jsonPath("$.onChainCosts.closeCosts", is("456")))
                .andExpect(jsonPath("$.balance.localBalance", is("1000")))
                .andExpect(jsonPath("$.balance.localReserve", is("100")))
                .andExpect(jsonPath("$.balance.localAvailable", is("900")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("123")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("10")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("113")))
                .andExpect(jsonPath("$.feeReport.earned", is("1234")))
                .andExpect(jsonPath("$.feeReport.sourced", is("567")))
                .andExpect(jsonPath("$.online", is(true)));
    }

    @Test
    void getOpenChannelIds() throws Exception {
        when(channelService.getOpenChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, LOCAL_OPEN_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/open-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getAllChannelIds() throws Exception {
        when(channelService.getAllChannelsWith(PUBKEY_2)).thenReturn(Set.of(LOCAL_OPEN_CHANNEL, CLOSED_CHANNEL_3));
        List<String> channelIds = List.of(CHANNEL_ID.toString(), CHANNEL_ID_3.toString());
        mockMvc.perform(get(NODE_PREFIX + "/all-channels"))
                .andExpect(jsonPath("$.node", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.channels", is(channelIds)));
    }

    @Test
    void getBalance() throws Exception {
        when(balanceService.getBalanceInformation(PUBKEY_2)).thenReturn(BALANCE_INFORMATION);
        mockMvc.perform(get(NODE_PREFIX + "/balance"))
                .andExpect(jsonPath("$.localBalance", is("1000")))
                .andExpect(jsonPath("$.localReserve", is("100")))
                .andExpect(jsonPath("$.localAvailable", is("900")))
                .andExpect(jsonPath("$.remoteBalance", is("123")))
                .andExpect(jsonPath("$.remoteReserve", is("10")))
                .andExpect(jsonPath("$.remoteAvailable", is("113")));
    }

    @Test
    void getFeeReport() throws Exception {
        when(feeService.getEarnedFeesForPeer(PUBKEY_2)).thenReturn(Coins.ofMilliSatoshis(1_234));
        when(feeService.getSourcedFeesForPeer(PUBKEY_2)).thenReturn(Coins.ofMilliSatoshis(567));
        mockMvc.perform(get(NODE_PREFIX + "/fee-report"))
                .andExpect(jsonPath("$.earned", is("1234")))
                .andExpect(jsonPath("$.sourced", is("567")));
    }
}