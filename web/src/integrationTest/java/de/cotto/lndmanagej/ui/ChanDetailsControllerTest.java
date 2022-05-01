package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.ui.controller.ChanDetailsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static de.cotto.lndmanagej.ui.model.ChanDetailsDtoFixture.CHAN_DETAILS_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChanDetailsController.class)
class ChanDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @SuppressWarnings("unused")
    @MockBean
    private ChannelIdConverter channelIdConverter;

    @Test
    void testNodeDetailsPage() throws Exception {
        given(this.pageService.channelDetails(any())).willReturn(new ChannelDetailsPage(CHAN_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/channel/783231610496155649"))
                .andExpect(status().isOk());
    }
}
