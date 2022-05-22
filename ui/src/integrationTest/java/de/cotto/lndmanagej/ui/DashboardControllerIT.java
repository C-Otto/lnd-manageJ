package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdResolver;
import de.cotto.lndmanagej.ui.controller.DashboardController;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @MockBean
    @SuppressWarnings("unused")
    private ChannelIdResolver channelIdResolver;

    @Test
    void empty_dashboard() throws Exception {
        when(pageService.dashboard()).thenReturn(new DashboardPage(List.of(), List.of(), NONE));
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void dashboard() throws Exception {
        OpenChannelDto channel = OPEN_CHANNEL_DTO;
        NodeDto node = new NodeDto(channel.remotePubkey().toString(), channel.remoteAlias(), true);
        when(pageService.dashboard()).thenReturn(
                new DashboardPage(List.of(channel), List.of(node), NONE)
        );
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }
}
