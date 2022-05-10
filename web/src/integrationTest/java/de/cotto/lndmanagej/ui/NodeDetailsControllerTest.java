package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.ui.controller.NodeDetailsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static de.cotto.lndmanagej.ui.model.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NodeDetailsController.class)
class NodeDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @SuppressWarnings("unused")
    @MockBean
    private ChannelIdConverter channelIdConverter;

    @Test
    void testNodeDetailsPage() throws Exception {
        given(this.pageService.nodeDetails(any())).willReturn(new NodeDetailsPage(NODE_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/node/" + NODE_DETAILS_DTO.node().toString()))
                .andExpect(status().isOk());
    }
}
