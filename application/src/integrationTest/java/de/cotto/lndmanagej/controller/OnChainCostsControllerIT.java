package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = OnChainCostsController.class)
class OnChainCostsControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OnChainCostService onChainCostService;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @Test
    void open_costs_for_channel() throws Exception {
        when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(123)));
        mockMvc.perform(get("/api/channel/" + CHANNEL_ID.getShortChannelId() + "/open-costs"))
                .andExpect(content().string("123"));
    }

    @Test
    void open_costs_for_channel_unknown() throws Exception {
        mockMvc.perform(get("/api/channel/" + CHANNEL_ID.getShortChannelId() + "/open-costs"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unable to get open costs for channel with ID " + CHANNEL_ID));
    }
}