package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.ui.controller.ChanDetailsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.ui.model.ChanDetailsDtoFixture.CHAN_DETAILS_DTO;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ChanDetailsController.class)
class ChanDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void testNodeDetailsPage() throws Exception {
        given(pageService.channelDetails(any())).willReturn(new ChannelDetailsPage(CHAN_DETAILS_DTO));
        mockMvc.perform(get("/channel/" + CHANNEL_ID))
                .andExpect(status().isOk());
    }
}
