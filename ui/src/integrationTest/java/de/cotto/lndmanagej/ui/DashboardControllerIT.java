package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.ui.controller.DashboardController;
import de.cotto.lndmanagej.ui.controller.param.SortBy;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO;
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

    @Test
    void dashboard_empty_okay() throws Exception {
        when(pageService.dashboard(SortBy.defaultSort)).thenReturn(new DashboardPage(List.of(), List.of(), NONE));
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void dashboard_unsupportedQueryParameter_badRequest() throws Exception {
        mockMvc.perform(get("/").param("sort", "unsupported-param"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dashboard_defaultSorting_ok() throws Exception {
        when(pageService.dashboard(SortBy.defaultSort)).thenReturn(
                new DashboardPage(List.of(OPEN_CHANNEL_DTO), List.of(NODE_DTO), NONE)
        );
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void dashboard_byAlias_ok() throws Exception {
        when(pageService.dashboard(SortBy.alias)).thenReturn(
                new DashboardPage(List.of(OPEN_CHANNEL_DTO), List.of(NODE_DTO), NONE)
        );
        mockMvc.perform(get("/").param("sort", "alias"))
                .andExpect(status().isOk());
    }

    @Test
    void channels_byRating_ok() throws Exception {
        when(pageService.channels(SortBy.channelrating)).thenReturn(new ChannelsPage(List.of(OPEN_CHANNEL_DTO)));
        mockMvc.perform(get("/channels/").param("sort", "channelrating"))
                .andExpect(status().isOk());
    }
}
