package de.cotto.lndmanagej.ui;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.ui.model.NodeDtoFixture;
import de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture;
import de.cotto.lndmanagej.ui.controller.SearchController;
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

import static de.cotto.lndmanagej.ui.model.ChanDetailsDtoFixture.CHAN_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.model.NodeDetailsDtoFixture.NODE_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static de.cotto.lndmanagej.ui.model.OpenChannelDtoFixture.WOS;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        given(this.dataService.getOpenChannels()).willReturn(List.of(OpenChannelDtoFixture.createFrom(CHAN_DETAILS_DTO)));
        given(this.pageService.channelDetails(any())).willReturn(new ChannelDetailsPage(CHAN_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=783231610496155649") )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", is(CHAN_DETAILS_DTO.channelId())))
                .andExpect(view().name("channel-details"));
    }

    @Test
    void searchForChannelId_viaCompactChannelId_found() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OpenChannelDtoFixture.createFrom(CHAN_DETAILS_DTO)));
        given(this.pageService.channelDetails(any())).willReturn(new ChannelDetailsPage(CHAN_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=712345x123x1") )
                .andExpect(status().isOk())
                .andExpect(model().attribute("id", is(CHAN_DETAILS_DTO.channelId())))
                .andExpect(view().name("channel-details"));
    }

    @Test
    void searchForPubkey_found() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OPEN_CHANNEL_DTO));
        given(this.pageService.nodeDetails(any())).willReturn(new NodeDetailsPage(NODE_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc121"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pubkey", is(OPEN_CHANNEL_DTO.remotePubkey())))
                .andExpect(view().name("node-details"));
    }

    @Test
    void searchForAlias_found() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OPEN_CHANNEL_DTO));
        given(this.pageService.nodeDetails(any())).willReturn(new NodeDetailsPage(NODE_DETAILS_DTO));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=albert"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pubkey", is(OPEN_CHANNEL_DTO.remotePubkey())))
                .andExpect(view().name("node-details"));
    }

    @Test
    void searchForAlias_TwoNodesFound() throws Exception {
        given(this.dataService.getOpenChannels()).willReturn(List.of(OPEN_CHANNEL_DTO, WOS)); //ALbert, wALletofsatotoshi
        given(this.pageService.nodes(any())).willReturn(new NodesPage(List.of(NodeDtoFixture.createFrom(OPEN_CHANNEL_DTO), NodeDtoFixture.createFrom(WOS))));
        mockMvc.perform(MockMvcRequestBuilders.get("/search?q=al"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("nodes"))
                .andExpect(view().name("nodes"));
    }

}
