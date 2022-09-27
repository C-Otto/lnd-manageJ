package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.RebalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = RebalancesController.class)
@Import(ChannelIdParser.class)
class RebalancesControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId() + "/";
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY + "/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private RebalanceService rebalanceService;

    @Test
    void getRebalanceSourceCostsForChannel() throws Exception {
        when(rebalanceService.getSourceCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(123));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-source-costs/"))
                .andExpect(content().string("123"));
    }

    @Test
    void getRebalanceSourceAmountForChannel() throws Exception {
        when(rebalanceService.getAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(456));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-source-amount/"))
                .andExpect(content().string("456"));
    }

    @Test
    void getRebalanceSourceCostsForPeer() throws Exception {
        when(rebalanceService.getSourceCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(124));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-source-costs/"))
                .andExpect(content().string("124"));
    }

    @Test
    void getRebalanceSourceAmountForPeer() throws Exception {
        when(rebalanceService.getAmountFromPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(666));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-source-amount/"))
                .andExpect(content().string("666"));
    }

    @Test
    void getRebalanceTargetCostsForChannel() throws Exception {
        when(rebalanceService.getTargetCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(125));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-target-costs/"))
                .andExpect(content().string("125"));
    }

    @Test
    void getRebalanceTargetAmountForChannel() throws Exception {
        when(rebalanceService.getAmountToChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(7777));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-target-amount/"))
                .andExpect(content().string("7777"));
    }

    @Test
    void getRebalanceTargetCostsForPeer() throws Exception {
        when(rebalanceService.getTargetCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(126));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-target-costs/"))
                .andExpect(content().string("126"));
    }

    @Test
    void getRebalanceTargetAmountForPeer() throws Exception {
        when(rebalanceService.getAmountToPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(999));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-target-amount/"))
                .andExpect(content().string("999"));
    }

    @Test
    void getRebalanceSupportAsSourceAmountForChannel() throws Exception {
        when(rebalanceService.getSupportAsSourceAmountFromChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(1));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-support-as-source-amount/"))
                .andExpect(content().string("1"));
    }

    @Test
    void getRebalanceSupportAsSourceAmountForPeer() throws Exception {
        when(rebalanceService.getSupportAsSourceAmountFromPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(2));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-support-as-source-amount/"))
                .andExpect(content().string("2"));
    }

    @Test
    void getRebalanceSupportAsTargetAmountForChannel() throws Exception {
        when(rebalanceService.getSupportAsTargetAmountToChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(3));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-support-as-target-amount/"))
                .andExpect(content().string("3"));
    }

    @Test
    void getRebalanceSupportAsTargetAmountForPeer() throws Exception {
        when(rebalanceService.getSupportAsTargetAmountToPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(4));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-support-as-target-amount/"))
                .andExpect(content().string("4"));
    }
}