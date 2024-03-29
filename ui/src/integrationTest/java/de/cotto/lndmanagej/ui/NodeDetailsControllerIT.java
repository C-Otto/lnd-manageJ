package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.controller.NodeDetailsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NodeDetailsController.class)
@Import(ChannelIdParser.class)
class NodeDetailsControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @Test
    void testNodeDetailsPage() throws Exception {
        when(pageService.nodeDetails(any())).thenReturn(new NodeDetailsPage(NODE_DETAILS_MODEL));
        mockMvc.perform(get("/node/" + NODE_DETAILS_MODEL.node().toString()))
                .andExpect(status().isOk());
    }
}
