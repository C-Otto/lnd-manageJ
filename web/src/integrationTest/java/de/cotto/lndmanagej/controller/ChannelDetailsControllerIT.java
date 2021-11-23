package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Coins;
import de.cotto.lndmanagej.service.BalanceService;
import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OnChainCostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static de.cotto.lndmanagej.model.BalanceInformationFixtures.BALANCE_INFORMATION_2;
import static de.cotto.lndmanagej.model.ChannelFixtures.CAPACITY;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL_PRIVATE;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChannelDetailsController.class)
class ChannelDetailsControllerIT {
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID.getShortChannelId();

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

    @Test
    void not_found() throws Exception {
        mockMvc.perform(get(CHANNEL_PREFIX + "/details"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getChannelDetails() throws Exception {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        when(channelService.getLocalChannel(CHANNEL_ID)).thenReturn(Optional.of(LOCAL_OPEN_CHANNEL_PRIVATE));
        when(onChainCostService.getOpenCosts(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(1000)));
        when(onChainCostService.getCloseCosts(CHANNEL_ID)).thenReturn(Optional.of(Coins.ofSatoshis(2000)));
        when(balanceService.getBalanceInformation(CHANNEL_ID)).thenReturn(Optional.of(BALANCE_INFORMATION_2));
        mockMvc.perform(get(CHANNEL_PREFIX + "/details"))
                .andExpect(jsonPath("$.channelIdShort", is(String.valueOf(CHANNEL_ID.getShortChannelId()))))
                .andExpect(jsonPath("$.channelIdCompact", is(CHANNEL_ID.getCompactForm())))
                .andExpect(jsonPath("$.channelIdCompactLnd", is(CHANNEL_ID.getCompactFormLnd())))
                .andExpect(jsonPath("$.channelPoint", is(CHANNEL_POINT.toString())))
                .andExpect(jsonPath("$.remotePubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.remoteAlias", is(ALIAS_2)))
                .andExpect(jsonPath("$.capacity", is(String.valueOf(CAPACITY.satoshis()))))
                .andExpect(jsonPath("$.openHeight", is(CHANNEL_ID.getBlockHeight())))
                .andExpect(jsonPath("$.private", is(true)))
                .andExpect(jsonPath("$.active", is(true)))
                .andExpect(jsonPath("$.onChainCosts.openCosts", is("1000")))
                .andExpect(jsonPath("$.onChainCosts.closeCosts", is("2000")))
                .andExpect(jsonPath("$.balance.localBalance", is("2000")))
                .andExpect(jsonPath("$.balance.localReserve", is("200")))
                .andExpect(jsonPath("$.balance.localAvailable", is("1800")))
                .andExpect(jsonPath("$.balance.remoteBalance", is("223")))
                .andExpect(jsonPath("$.balance.remoteReserve", is("20")))
                .andExpect(jsonPath("$.balance.remoteAvailable", is("203")));
    }

    @Test
    void getChannelDetails_channel_not_found() throws Exception {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        mockMvc.perform(get(CHANNEL_PREFIX + "/details"))
                .andExpect(status().isNotFound());
    }
}