package de.cotto.lndmanagej.ui.controller;

import de.cotto.lndmanagej.ui.dto.NodeDto;
import de.cotto.lndmanagej.ui.page.PageService;
import de.cotto.lndmanagej.ui.page.channel.ChannelsPage;
import de.cotto.lndmanagej.ui.page.general.DashboardPage;
import de.cotto.lndmanagej.ui.page.node.NodesPage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;

import static de.cotto.lndmanagej.model.NodeFixtures.NODE_PEER;
import static de.cotto.lndmanagej.model.PubkeyFixtures.PUBKEY;
import static de.cotto.lndmanagej.model.RatingFixtures.RATING;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.DEFAULT_SORT;
import static de.cotto.lndmanagej.ui.controller.param.SortBy.RATIO;
import static de.cotto.lndmanagej.ui.dto.OpenChannelDtoFixture.OPEN_CHANNEL_DTO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    private static final String CHANNELS_KEY = "channels";
    private static final String NODES_KEY = "nodes";

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private PageService pageService;

    @Mock
    private Model model;

    @Test
    void dashboard() {
        when(pageService.dashboard(DEFAULT_SORT)).thenReturn(new DashboardPage(List.of(), List.of(), List.of()));
        assertThat(dashboardController.dashboard(model, DEFAULT_SORT)).isEqualTo("dashboard");
        verify(model).addAllAttributes(
                Map.of(NODES_KEY, List.of(), CHANNELS_KEY, List.of(), "warnings", List.of())
        );
    }

    @Test
    void dashboard_forwards_sort_key_to_page() {
        when(pageService.dashboard(any())).thenReturn(new DashboardPage(List.of(), List.of(), List.of()));
        dashboardController.dashboard(model, RATIO);
        verify(pageService).dashboard(RATIO);
    }

    @Test
    void channels() {
        when(pageService.channels(DEFAULT_SORT)).thenReturn(new ChannelsPage(List.of(OPEN_CHANNEL_DTO)));
        assertThat(dashboardController.channels(model, DEFAULT_SORT)).isEqualTo(CHANNELS_KEY);
        verify(model).addAllAttributes(Map.of(CHANNELS_KEY, List.of(OPEN_CHANNEL_DTO)));
    }

    @Test
    void channels_forwards_sort_key_to_page() {
        when(pageService.channels(any())).thenReturn(new ChannelsPage(List.of()));
        dashboardController.channels(model, RATIO);
        verify(pageService).channels(RATIO);
    }

    @Test
    void nodes() {
        NodeDto nodeDto = new NodeDto(PUBKEY.toString(), NODE_PEER.alias(), true, RATING.getValue());
        when(pageService.nodes(DEFAULT_SORT)).thenReturn(new NodesPage(List.of(nodeDto)));
        assertThat(dashboardController.nodes(model, DEFAULT_SORT)).isEqualTo(NODES_KEY);
        verify(model).addAllAttributes(Map.of(NODES_KEY, List.of(nodeDto)));
    }
}
