package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.OnChainCostsFixtures.ON_CHAIN_COSTS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OnChainCostsController.class)
class OnChainCostsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();
    private static final String PEER_PREFIX = "/api/node/" + PUBKEY;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private OnChainCostService onChainCostService;

    @Test
    void on_chain_costs_for_peer() throws Exception {
        when(onChainCostService.getOnChainCostsForPeer(PUBKEY)).thenReturn(ON_CHAIN_COSTS);
        mockMvc.perform(get(PEER_PREFIX + "/on-chain-costs"))
                .andExpect(jsonPath("$.openCosts", is("1000")))
                .andExpect(jsonPath("$.closeCosts", is("2000")))
                .andExpect(jsonPath("$.sweepCosts", is("3000")));
    }

    @Test
    void open_costs_for_channel() throws Exception {
        when(onChainCostService.getOpenCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        mockMvc.perform(get(CHANNEL_PREFIX + "/open-costs"))
                .andExpect(content().string("123"));
    }

    @Test
    void open_costs_for_channel_unknown() throws Exception {
        mockMvc.perform(get(CHANNEL_PREFIX + "/open-costs"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unable to get open costs for channel with ID " + CHANNEL_ID));
    }

    @Test
    void close_costs_for_channel() throws Exception {
        when(onChainCostService.getCloseCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        mockMvc.perform(get(CHANNEL_PREFIX + "/close-costs"))
                .andExpect(content().string("123"));
    }

    @Test
    void sweep_costs_for_channel() throws Exception {
        when(onChainCostService.getSweepCostsForChannelId(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        mockMvc.perform(get(CHANNEL_PREFIX + "/sweep-costs"))
                .andExpect(content().string("123"));
    }

    @Test
    void close_costs_for_channel_unknown() throws Exception {
        mockMvc.perform(get(CHANNEL_PREFIX + "/close-costs"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unable to get close costs for channel with ID " + CHANNEL_ID));
    }

    @Test
    void sweep_costs_channel_unknown() throws Exception {
        mockMvc.perform(get(CHANNEL_PREFIX + "/sweep-costs"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unable to get sweep costs for channel with ID " + CHANNEL_ID));
    }
}