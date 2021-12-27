package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.model.NodeWarnings;
import de.cotto.lndmanagej.service.NodeWarningsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.NodeWarningsFixtures.NODE_WARNINGS;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = WarningsController.class)
class WarningsControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY;

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
        mockMvc.perform(get(NODE_PREFIX + "/warnings"))
                .andExpect(jsonPath("$.nodeWarnings[0].onlinePercentage", is(51)));
    }

    @Test
    void getWarningsForNode_empty() throws Exception {
        when(nodeWarningsService.getNodeWarnings(PUBKEY)).thenReturn(NodeWarnings.NONE);
        mockMvc.perform(get(NODE_PREFIX + "/warnings"))
                .andExpect(jsonPath("$.nodeWarnings", hasSize(0)));
    }
}