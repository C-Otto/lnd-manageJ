package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OffChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = OffChainCostsController.class)
class OffChainCostsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId() + "/";
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY + "/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private OffChainCostService offChainCostService;

    @Test
    void getRebalanceSourceCostsForChannel() throws Exception {
        when(offChainCostService.getRebalanceSourceCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(123));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-source-costs/"))
                .andExpect(content().string("123"));
    }

    @Test
    void getRebalanceSourceCostsForPeer() throws Exception {
        when(offChainCostService.getRebalanceSourceCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(124));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-source-costs/"))
                .andExpect(content().string("124"));
    }

    @Test
    void getRebalanceTargetCostsForChannel() throws Exception {
        when(offChainCostService.getRebalanceTargetCostsForChannel(CHANNEL_ID)).thenReturn(Coins.ofMilliSatoshis(125));
        mockMvc.perform(get(CHANNEL_PREFIX + "/rebalance-target-costs/"))
                .andExpect(content().string("125"));
    }

    @Test
    void getRebalanceTargetCostsForPeer() throws Exception {
        when(offChainCostService.getRebalanceTargetCostsForPeer(PUBKEY)).thenReturn(Coins.ofMilliSatoshis(126));
        mockMvc.perform(get(NODE_PREFIX + "/rebalance-target-costs/"))
                .andExpect(content().string("126"));
    }
}