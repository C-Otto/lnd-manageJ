package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_3;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_2;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE_3;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_3;
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
    private static final String WARNINGS = "/warnings";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @MockBean
    private NodeWarningsService nodeWarningsService;

    @Test
    void getWarningsForNode() throws Exception {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NODE_WARNINGS);
        mockMvc.perform(get(NODE_PREFIX + WARNINGS))
                .andExpect(jsonPath("$.nodeWarnings", containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                )));
    }

    @Test
    void getWarningsForNode_empty() throws Exception {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NodeWarnings.NONE);
        mockMvc.perform(get(NODE_PREFIX + WARNINGS))
                .andExpect(jsonPath("$.nodeWarnings", hasSize(0)));
    }

    @Test
    void getWarnings_empty() throws Exception {
        when(nodeWarningsService.getNodeWarnings()).thenReturn(Map.of());
        mockMvc.perform(get("/api" + WARNINGS))
                .andExpect(jsonPath("$.nodesWithWarnings", hasSize(0)));
    }

    @Test
    void getWarnings() throws Exception {
        when(nodeWarningsService.getNodeWarnings())
                .thenReturn(Map.of(NODE_2, NODE_WARNINGS, NODE_3, NODE_WARNINGS_2));
        mockMvc.perform(get("/api" + WARNINGS))
                .andExpect(jsonPath("$.nodesWithWarnings[0].alias", is(ALIAS_2)))
                .andExpect(jsonPath("$.nodesWithWarnings[0].pubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.nodesWithWarnings[0].nodeWarnings", containsInAnyOrder(
                        "No flow in the past 16 days",
                        "Node has been online 51% in the past 14 days",
                        "Node changed between online and offline 123 times in the past 7 days"
                )))
                .andExpect(jsonPath("$.nodesWithWarnings[1].alias", is(ALIAS_3)))
                .andExpect(jsonPath("$.nodesWithWarnings[1].pubkey", is(PUBKEY_3.toString())))
                .andExpect(jsonPath("$.nodesWithWarnings[1].nodeWarnings", containsInAnyOrder(
                        "Node has been online 1% in the past 21 days",
                        "Node changed between online and offline 99 times in the past 14 days"
                )));
    }
}