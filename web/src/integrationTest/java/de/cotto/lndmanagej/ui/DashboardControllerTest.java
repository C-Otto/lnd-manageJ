package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.ui.controller.DashboardController;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.dto.StatusModel;
import de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static de.cotto.lndmanagej.controller.dto.NodesAndChannelsWithWarningsDto.NONE;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PageService pageService;

    @SuppressWarnings("unused")
    @MockBean
    private ChannelIdConverter channelIdConverter;

    @Test
    void testEmptyDashboard() throws Exception {
        given(this.pageService.dashboard()).willReturn(
                new DashboardPage(List.of(), List.of(), new StatusModel(true, 1, NONE))
        );
        mockMvc.perform(MockMvcRequestBuilders.get("/")).andExpect(status().isOk());
    }

    @Test
    void testDashboard() throws Exception {
        OpenChannelDto channel = OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
        NodeDto node = new NodeDto(channel.remotePubkey().toString(), channel.remoteAlias(), true);
        given(this.pageService.dashboard()).willReturn(
                new DashboardPage(List.of(channel), List.of(node), new StatusModel(true, 1, NONE))
        );
        mockMvc.perform(MockMvcRequestBuilders.get("/")).andExpect(status().isOk());
    }
}
