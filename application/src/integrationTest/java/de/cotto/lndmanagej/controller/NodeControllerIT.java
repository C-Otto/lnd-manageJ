package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.model.Node;
import de.cotto.lndmanagej.service.NodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS_2;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY_2;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = NodeController.class)
class NodeControllerIT {
    private static final String NODE_PREFIX = "/api/node/" + PUBKEY_2;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NodeService nodeService;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @Test
    void getAlias() throws Exception {
        when(nodeService.getAlias(PUBKEY_2)).thenReturn(ALIAS_2);
        mockMvc.perform(get(NODE_PREFIX + "/alias"))
                .andExpect(content().string(ALIAS_2));
    }

    @Test
    void getDetails() throws Exception {
        when(nodeService.getNode(PUBKEY_2)).thenReturn(new Node(PUBKEY_2, ALIAS_2, 0, true));
        mockMvc.perform(get(NODE_PREFIX + "/details"))
                .andExpect(jsonPath("$.pubkey", is(PUBKEY_2.toString())))
                .andExpect(jsonPath("$.alias", is(ALIAS_2)))
                .andExpect(jsonPath("$.online", is(true)));
    }
}