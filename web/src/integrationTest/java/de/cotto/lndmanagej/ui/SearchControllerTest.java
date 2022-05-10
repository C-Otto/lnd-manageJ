package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static de.cotto.lndmanagej.ui.model.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.model.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UiDataService dataService;

    @MockBean
    private PageService pageService;

    @SuppressWarnings("unused")
    @MockBean
    private ChannelIdConverter channelIdConverter;

    @Test
    void search_noOpenChannels_errorPage() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of());
        given(this.pageService.error(any())).willReturn(new ErrorPage("myErrorMessage"));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=783231610496155649"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "myErrorMessage"))
                .andExpect(view().name("error"));
    }

    @Test
    void searchForChannelId_viaShortChannelId_found() throws Exception {
        searchForChannelId("783231610496155649");
    }

    @Test
    void searchForChannelId_viaCompactChannelId_found() throws Exception {
        searchForChannelId("712345x123x1");
    }

    private void searchForChannelId(String query) throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(
                List.of(create(CHANNEL_DETAILS_DTO))
        );
        given(this.pageService.channelDetails(any())).willReturn(new ChannelDetailsPage(CHANNEL_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=" + query))
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", is(CHANNEL_DETAILS_DTO.channelId())))
                .andExpect(view().name("channel-details"));
    }

    @Test
    void searchForPubkey_found() throws Exception {
        searchAndExpectSingleNode(NODE_DETAILS_DTO.node().toString());
    }

    @Test
    void searchForAlias_found() throws Exception {
        searchAndExpectSingleNode("albert");
    }

    private void searchAndExpectSingleNode(String query) throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OPEN_CHANNEL_DTO));
        given(this.pageService.nodeDetails(any())).willReturn(new NodeDetailsPage(NODE_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=" + query))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pubkey", is(OPEN_CHANNEL_DTO.remotePubkey())))
                .andExpect(view().name("node-details"));
    }

    @Test
    void searchForAlias_TwoNodesFound() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OPEN_CHANNEL_DTO, WOS));
        given(this.pageService.nodes(any())).willReturn(nodesPage(OPEN_CHANNEL_DTO, WOS)
        );
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=al"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodes"))
                .andExpect(view().name("nodes"));
    }

    private NodesPage nodesPage(OpenChannelDto channel1, OpenChannelDto channel2) {
        return new NodesPage(List.of(create(channel1), create(channel2)));
    }

    public static OpenChannelDto create(ChannelDetailsDto channelDetails) {
        return new OpenChannelDto(
                channelDetails.channelId(),
                channelDetails.remoteAlias(),
                channelDetails.remotePubkey(),
                channelDetails.policies(),
                channelDetails.balanceInformation());
    }

    public static NodeDto create(OpenChannelDto channel) {
        return new NodeDto(channel.remotePubkey().toString(), channel.remoteAlias(), true);
    }

}
