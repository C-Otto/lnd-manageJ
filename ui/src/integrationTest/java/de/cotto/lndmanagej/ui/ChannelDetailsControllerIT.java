package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.controller.ChannelDetailsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChannelDetailsController.class)
@Import(ChannelIdParser.class)
class ChannelDetailsControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @Test
    void node_details_page() throws Exception {
        when(pageService.channelDetails(any())).thenReturn(new ChannelDetailsPage(CHANNEL_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/channel/" + CHANNEL_ID))
                .andExpect(status().isOk());
    }
}
