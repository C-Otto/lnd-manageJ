package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.controller.ChannelIdConverter;
import de.cotto.lndmanagej.controller.NotFoundException;
import de.cotto.lndmanagej.ui.UiDataService;
import de.cotto.lndmanagej.ui.dto.OpenChannelDto;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelDetailsPage;
import de.cotto.lndmanagej.ui.page.general.ErrorPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static de.cotto.lndmanagej.model.ChannelIdFixtures.CHANNEL_ID;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.ui.dto.ChannelDetailsDtoFixture.CHANNEL_DETAILS_DTO;
import static de.cotto.lndmanagej.ui.dto.NodeDtoFixture.NODE_DTO;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {
    @InjectMocks
    private SearchController searchController;

    @Mock
    private PageService pageService;

    @Mock
    private UiDataService dataService;

    @Mock
    private ChannelIdConverter channelIdConverter;

    @Mock
    private Model model;

    @Test
    void by_channel_id() throws NotFoundException {
        String query = "xyz";
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        when(pageService.channelDetails(CHANNEL_ID)).thenReturn(new ChannelDetailsPage(CHANNEL_DETAILS_DTO));
        when(channelIdConverter.tryToConvert(query)).thenReturn(Optional.of(CHANNEL_ID));
        assertThat(searchController.search(query, model)).isEqualTo("channel-details");
    }

    @Test
    void by_channel_id_details_not_found() throws NotFoundException {
        String query = "abc";
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        when(channelIdConverter.tryToConvert(query)).thenReturn(Optional.of(CHANNEL_ID));
        when(pageService.channelDetails(CHANNEL_ID)).thenThrow(NotFoundException.class);
        when(pageService.error("Channel not found.")).thenReturn(new ErrorPage("x"));
        assertThat(searchController.search(query, model)).isEqualTo("error");
    }

    @Test
    void by_pubkey() {
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        assertThat(searchController.search(PUBKEY.toString(), model))
                .isEqualTo("redirect:/node/" + PUBKEY);
    }

    @Test
    void by_alias_infix_one_result_redirect() {
        String query = "BERT";
        when(dataService.getOpenChannels()).thenReturn(List.of(OPEN_CHANNEL_DTO));
        assertThat(searchController.search(query, model))
                .isEqualTo("redirect:/node/027abc123abc123abc123abc123123abc123abc123abc123abc123abc123abc121");
    }

    @Test
    void by_alias_infix_two_results() {
        String query = "BERT";
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO, OPEN_CHANNEL_DTO);
        when(dataService.getOpenChannels()).thenReturn(channels);
        when(pageService.nodes(channels)).thenReturn(new NodesPage(List.of(NODE_DTO)));
        assertThat(searchController.search(query, model)).isEqualTo("nodes");
    }

    @Test
    void by_alias_infix_no_result() {
        String query = "ernie";
        List<OpenChannelDto> channels = List.of(OPEN_CHANNEL_DTO);
        when(dataService.getOpenChannels()).thenReturn(channels);
        when(pageService.error("No search result.")).thenReturn(new ErrorPage("x"));
        assertThat(searchController.search(query, model)).isEqualTo("error");
    }
}
