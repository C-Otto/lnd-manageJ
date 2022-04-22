package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.controller.PendingChannelsController;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.PendingChannelsPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.ui.dto.PendingOpenChannelDtoFixture.PENDING_OPEN_CHANNEL_DTO;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PendingChannelsController.class)
@Import(ChannelIdParser.class)
class PendingChannelsControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @Test
    void pendingChannelsPage_noPendingChannel_okay() throws Exception {
        when(pageService.pendingChannels()).thenReturn(new PendingChannelsPage(List.of()));
        mockMvc.perform(get("/pending-channels"))
                .andExpect(status().isOk());
    }

    @Test
    void pendingChannelsPage_hasPendingChannel_containsAlbertAlias() throws Exception {
        when(pageService.pendingChannels()).thenReturn(new PendingChannelsPage(List.of(PENDING_OPEN_CHANNEL_DTO)));
        mockMvc.perform(get("/pending-channels"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Albert")));
    }
}
