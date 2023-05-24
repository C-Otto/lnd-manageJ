package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.model.ChannelIdParser;
import de.cotto.lndmanagej.ui.controller.SearchController;
import de.cotto.lndmanagej.ui.dto.ChannelDetailsDto;
import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodeDetailsPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.ChannelPointFixtures.CHANNEL_POINT;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.DEFAULT_SORT;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDetailsDtoFixture.NODE_DETAILS_MODEL;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO2;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = SearchController.class)
@Import(ChannelIdParser.class)
class SearchControllerIT extends BaseControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UiDataService dataService;

    @MockBean
    private PageService pageService;

    @Test
    void search_noOpenChannels_errorPage() throws Exception {
        when(dataService.getOpenChannels()).thenReturn(List.of());
        when(pageService.error(any())).thenReturn(new ErrorPage("myErrorMessage"));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=" + CHANNEL_ID))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "myErrorMessage"))
                .andExpect(view().name("error-page"));
    }

    @Test
    void searchForChannelId_viaShortChannelId_found() throws Exception {
        searchForChannelId(CHANNEL_ID.getShortChannelId());
    }

    @Test
    void searchForChannelId_viaCompactChannelId_found() throws Exception {
        searchForChannelId(CHANNEL_ID);
    }

    @Test
    void searchForChannelId_viaChannelPoint_found() throws Exception {
        when(getChannelIdResolverMockBean().resolveFromChannelPoint(CHANNEL_POINT)).thenReturn(Optional.of(CHANNEL_ID));
        searchForChannelId(CHANNEL_POINT.toString());
    }

    private void searchForChannelId(Object query) throws Exception {
        when(dataService.getOpenChannels()).thenReturn(
                List.of(openChannelDto(CHANNEL_DETAILS_DTO))
        );
        when(pageService.channelDetails(any())).thenReturn(new ChannelDetailsPage(CHANNEL_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=" + query))
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", is(CHANNEL_DETAILS_DTO.channelId())))
                .andExpect(view().name("channel-details-page"));
    }

    @Test
    void searchForPubkey_found() throws Exception {
        searchAndExpectRedirect(NODE_DETAILS_MODEL.node().toString());
    }

    @Test
    void searchForAlias_found() throws Exception {
        searchAndExpectRedirect("albert");
    }

    private void searchAndExpectRedirect(String query) throws Exception {
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        when(pageService.nodeDetails(any())).thenReturn(new NodeDetailsPage(NODE_DETAILS_MODEL));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=" + query))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void searchForAlias_multipleResults() throws Exception {
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO, OPEN_CHANNEL_DTO2));
        when(pageService.nodes(any(), eq(DEFAULT_SORT))).thenReturn(nodesPage(OPEN_CHANNEL_DTO, OPEN_CHANNEL_DTO2));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=alb"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodes"))
                .andExpect(view().name("nodes-page"));
    }

    private NodesPage nodesPage(OpenChannelDto... channels) {
        return new NodesPage(Arrays.stream(channels).map(this::nodeDto).toList());
    }

    public OpenChannelDto openChannelDto(ChannelDetailsDto channelDetails) {
        return new OpenChannelDto(
                channelDetails.channelId(),
                channelDetails.remoteAlias(),
                channelDetails.remotePubkey(),
                channelDetails.policies(),
                channelDetails.balanceInformation(),
                channelDetails.capacitySat(),
                false,
                1234);
    }

    public NodeDto nodeDto(OpenChannelDto channel) {
        return new NodeDto(channel.remotePubkey().toString(), channel.remoteAlias(), true, 1234);
    }

}
