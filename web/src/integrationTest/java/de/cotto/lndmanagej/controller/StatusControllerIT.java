package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.metrics.Metrics;
import de.cotto.lndmanagej.service.OwnNodeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = StatusController.class)
class StatusControllerIT {
    private static final String PREFIX = "/api/status/";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @SuppressWarnings("unused")
    private Metrics metrics;

    @MockBean
    private OwnNodeService ownNodeService;

    @Test
    void isSyncedToChain() throws Exception {
        when(ownNodeService.isSyncedToChain()).thenReturn(true);
        mockMvc.perform(get(PREFIX + "/synced-to-chain"))
                .andExpect(content().string("true"));
    }
}