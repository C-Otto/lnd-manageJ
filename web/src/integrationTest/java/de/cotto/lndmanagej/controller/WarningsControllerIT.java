package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.warnings.ChannelWarnings;
import de.cotto.lndmanagej.model.warnings.NodeWarnings;
import de.cotto.lndmanagej.service.ChannelWarningsService;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.LocalOpenChannelFixtures.LOCAL_OPEN_CHANNEL;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
import static de.cotto.lndmanagej.model.warnings.ChannelWarningsFixtures.CHANNEL_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.model.warnings.NodeWarningsFixtures.NODE_WARNINGS_2;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = WarningsController.class)
class WarningsControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;
    private static final String CHANNEL_PREFIX = "/api/channel/" + CHANNEL_ID;
    private static final String WARNINGS = "/warnings";
    private static final String WARNINGS_PATH = "$.warnings";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private NodeWarningsService nodeWarningsService;

    @MockBean
    private ChannelWarningsService channelWarningsService;

    @Test
    void getWarningsForNode() throws Exception {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        mockMvc.perform(get(NODE_PREFIX + WARNINGS))
                .andExpect(jsonPath(WARNINGS_PATH, containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                )));
    }

    @Test
    void getWarningsForNode_empty() throws Exception {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NodeWarnings.NONE);
        mockMvc.perform(get(NODE_PREFIX + WARNINGS))
                .andExpect(jsonPath(WARNINGS_PATH, hasSize(0)));
    }

    @Test
    void getWarningsForChannel() throws Exception {
        when(channelWarningsService.getChannelWarnings(CHANNEL_ID)).thenReturn(CHANNEL_WARNINGS);
        mockMvc.perform(get(CHANNEL_PREFIX + WARNINGS))
                .andExpect(jsonPath(WARNINGS_PATH, containsInAnyOrder(
                        "Channel has accumulated 101,000 updates"
                )));
    }

    @Test
    void getWarningsForChannel_empty() throws Exception {
        when(channelWarningsService.getChannelWarnings(CHANNEL_ID)).thenReturn(ChannelWarnings.NONE);
        mockMvc.perform(get(CHANNEL_PREFIX + WARNINGS))
                .andExpect(jsonPath(WARNINGS_PATH, hasSize(0)));
    }

    @Test
    void getWarnings_empty() throws Exception {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        mockMvc.perform(get("/api" + WARNINGS))
                .andExpect(jsonPath("$.nodesWithWarnings", hasSize(0)))
                .andExpect(jsonPath("$.channelsWithWarnings", hasSize(0)));
    }

    @Test
    void getWarnings() throws Exception {
        when(nodeWarningsService.getNodeWarnings())
                .thenReturn(Map.of(NODE_2, NODE_WARNINGS, NODE_3, NODE_WARNINGS_2));
        when(channelWarningsService.getChannelWarnings())
                .thenReturn(Map.of(LOCAL_OPEN_CHANNEL, CHANNEL_WARNINGS));
        mockMvc.perform(get("/api" + WARNINGS))
                .andExpect(jsonPath("$.nodesWithWarnings[0].alias", is(ALIAS_2)))
                .andExpect(jsonPath("$.nodesWithWarnings[0].pubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.nodesWithWarnings[0].warnings", containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                )))
                .andExpect(jsonPath("$.nodesWithWarnings[1].alias", is(ALIAS_3)))
                .andExpect(jsonPath("$.nodesWithWarnings[1].pubkey", is(PUBKEY_3.toString())))
                .andExpect(jsonPath("$.nodesWithWarnings[1].warnings", containsInAnyOrder(
                        "Node has been online 1% in the past 21 days",
                        "Node changed between online and offline 99 times in the past 14 days"
                )))
                .andExpect(jsonPath("$.channelsWithWarnings[0].channelId", is(CHANNEL_ID.toString())))
                .andExpect(jsonPath("$.channelsWithWarnings[0].warnings", containsInAnyOrder(
                        "Channel has accumulated 101,000 updates"
                )));
    }
}