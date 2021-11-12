package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.service.ChannelService;
import de.cotto.lndmanagej.service.NodeService;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID_3;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
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
        when(channelService.getOpenChannelsWith(PUBKEY)).thenReturn(List.of(CHANNEL_ID, CHANNEL_ID_3));
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
}