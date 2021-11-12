package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.FeeService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_3;
import static de.cotto.lndmanagej.model.LocalChannelFixtures.LOCAL_CHANNEL_TO_NODE_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LegacyController.class)
class LegacyControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @MockBean
    private ChannelService channelService;

    @MockBean
    private OwnNodeService ownNodeService;

    @MockBean
    private FeeService feeService;

    @Test
    void getAlias() throws Exception {
        when(nodeService.getAlias(PUBKEY)).thenReturn(ALIAS);
        mockMvc.perform(get("/legacy/node/" + PUBKEY + "/alias")).andExpect(content().string(ALIAS));
    }

    @Test
    void getAlias_error() throws Exception {
        mockMvc.perform(get("/legacy/node/xxx/alias")).andExpect(status().isBadRequest());
    }

    @Test
    void getOpenChannelIds() throws Exception {
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_3));
        mockMvc.perform(get("/legacy/node/" + PUBKEY + "/open-channels"))
                .andExpect(content().string(CHANNEL_ID + "\n" + CHANNEL_ID_3));
    }

    @Test
    void isSyncedToChain_true() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        mockMvc.perform(get("/legacy/synced-to-chain")).andExpect(content().string("true"));
    }

    @Test
    void isSyncedToChain_false() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(false);
        mockMvc.perform(get("/legacy/synced-to-chain")).andExpect(content().string("false"));
    }

    @Test
    void getPeerPubkeys() throws Exception {
        when(channelService.getOpenChannels()).thenReturn(Set.of(LOCAL_CHANNEL, LOCAL_CHANNEL_TO_NODE_3));
        mockMvc.perform(get("/legacy/peer-pubkeys"))
                .andExpect(content().string(PUBKEY_2 + "\n" + PUBKEY_3));
    }

    @Test
    void getIncomingFeeRate() throws Exception {
        when(feeService.getIncomingFeeRate(CHANNEL_ID)).thenReturn(123L);
        mockMvc.perform(get("/legacy/channel/" + CHANNEL_ID + "/incoming-fee-rate"))
                .andExpect(content().string("123"));
    }

    @Test
    void getOutgoingFeeRate() throws Exception {
        when(feeService.getOutgoingFeeRate(CHANNEL_ID)).thenReturn(123L);
        mockMvc.perform(get("/legacy/channel/" + CHANNEL_ID + "/outgoing-fee-rate"))
                .andExpect(content().string("123"));
    }
}