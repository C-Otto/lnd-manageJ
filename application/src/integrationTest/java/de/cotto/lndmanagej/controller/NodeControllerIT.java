package de.cotto.lndmanagej.controller;

import de.cotto.lndmanagej.grpc.GrpcNodeInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.NodeFixtures.ALIAS;
import static de.cotto.lndmanagej.model.NodeFixtures.NODE;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(controllers = NodeController.class)
class NodeControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GrpcNodeInfo grpcNodeInfo;

    @Test
    void getAlias() throws Exception {
        when(grpcNodeInfo.getNode(PUBKEY)).thenReturn(NODE);
        mockMvc.perform(get("/api/node/" + PUBKEY + "/alias")).andExpect(content().string(ALIAS));
    }
}