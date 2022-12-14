package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.controller.DashboardController;
import de.cotto.lndmanagej.ui.controller.param.SortBy;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static de.cotto.lndmanagej.ui.controller.param.SortBy.SORT_PARAM_KEY;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
@Import(ChannelIdParser.class)
class DashboardControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @Test
    void dashboard_empty_okay() throws Exception {
        when(pageService.dashboard(SortBy.DEFAULT_SORT)).thenReturn(new DashboardPage(List.of(), List.of(), List.of()));
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void dashboard_defaultSorting_ok() throws Exception {
        when(pageService.dashboard(SortBy.DEFAULT_SORT)).thenReturn(
                new DashboardPage(List.of(OPEN_CHANNEL_DTO), List.of(NODE_DTO), List.of())
        );
        mockMvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void dashboard_byAlias_ok() throws Exception {
        when(pageService.dashboard(SortBy.ALIAS)).thenReturn(
                new DashboardPage(List.of(OPEN_CHANNEL_DTO), List.of(NODE_DTO), List.of())
        );
        mockMvc.perform(get("/").param(SORT_PARAM_KEY, "alias"))
                .andExpect(status().isOk());
    }

    @Test
    void dashboard_byNodeAlias_ok() throws Exception {
        when(pageService.dashboard(SortBy.NODE_ALIAS)).thenReturn(
                new DashboardPage(List.of(OPEN_CHANNEL_DTO), List.of(NODE_DTO), List.of())
        );
        mockMvc.perform(get("/").param("sort", "node-alias"))
                .andExpect(status().isOk());
    }

    @Test
    void channels_byRating_ok() throws Exception {
        when(pageService.channels(SortBy.RATING)).thenReturn(new ChannelsPage(List.of(OPEN_CHANNEL_DTO)));
        mockMvc.perform(get("/channels").param(SORT_PARAM_KEY, "rating"))
                .andExpect(status().isOk());
    }

    @Test
    void nodes_byRating_ok() throws Exception {
        when(pageService.nodes(SortBy.NODE_RATING)).thenReturn(new NodesPage(List.of(NODE_DTO)));
        mockMvc.perform(get("/nodes").param(SORT_PARAM_KEY, "node-rating"))
                .andExpect(status().isOk());
    }
}
